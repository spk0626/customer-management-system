package com.customermgmt.service.impl;

import com.customermgmt.dto.response.BulkJobResponse;
import com.customermgmt.entity.BulkUploadJob;
import com.customermgmt.entity.Customer;
import com.customermgmt.exception.ResourceNotFoundException;
import com.customermgmt.mapper.BulkUploadJobMapper;
import com.customermgmt.repository.BulkUploadJobRepository;
import com.customermgmt.repository.CustomerRepository;
import com.customermgmt.service.BulkUploadService;
import com.customermgmt.util.ExcelRowHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFStylesTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkUploadServiceImpl implements BulkUploadService {

    private final BulkUploadJobRepository jobRepository;
    private final CustomerRepository customerRepository;
    private final BulkUploadJobMapper jobMapper;

    @Value("${bulk.processing.chunk-size:500}")
    private int chunkSize;

    @Override
    @Transactional
    public BulkJobResponse submitBulkUpload(MultipartFile file) {
        validateFile(file);

        // Copy file bytes to memory once — avoids repeated stream reads
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read uploaded file: " + e.getMessage());
        }

        BulkUploadJob job = new BulkUploadJob();
        job.setFileName(file.getOriginalFilename());
        job.setStatus(BulkUploadJob.Status.PENDING);
        BulkUploadJob savedJob = jobRepository.save(job);

        // Kick off async — returns immediately to caller
        processAsync(savedJob.getId(), fileBytes);

        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public BulkJobResponse getJobStatus(Long jobId) {
        BulkUploadJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("BulkUploadJob", jobId));
        return jobMapper.toResponse(job);
    }

    // -------------------------------------------------------------------------
    // Async processing — runs in bulkTaskExecutor thread pool
    // -------------------------------------------------------------------------

    /**
     * Uses Apache POI's SAX (event-driven) parser — this is critical for 1M rows.
     *
     * DOM-based parsing (XSSFWorkbook) loads the entire file into memory.
     * At ~200 bytes/row that's 200MB of heap just for the parse tree.
     * SAX fires events row-by-row so memory usage stays constant regardless of file size.
     *
     * Rows are collected into chunks and flushed to DB with JDBC batch inserts,
     * keeping transaction scope small and GC pressure low.
     */
    @Async("bulkTaskExecutor")
    public void processAsync(Long jobId, byte[] fileBytes) {
        BulkUploadJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(BulkUploadJob.Status.PROCESSING);
        job.setStartedAt(LocalDateTime.now());
        jobRepository.save(job);

        List<String[]> chunk = new ArrayList<>(chunkSize);
        int[] counters = {0, 0, 0}; // [total, processed, failed]
        boolean[] headerSkipped = {false};

        try (InputStream is = new java.io.ByteArrayInputStream(fileBytes)) {
            OPCPackage pkg = OPCPackage.open(is);
            XSSFReader reader = new XSSFReader(pkg);
            XSSFSharedStringsTable sst = new XSSFSharedStringsTable(pkg);
            XSSFStylesTable styles = reader.getStylesTable();

            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            ExcelRowHandler handler = new ExcelRowHandler(sst, styles, row -> {
                if (!headerSkipped[0]) {
                    headerSkipped[0] = true; // skip header row
                    return;
                }
                counters[0]++;
                chunk.add(row);
                if (chunk.size() >= chunkSize) {
                    int[] result = persistChunk(new ArrayList<>(chunk));
                    counters[1] += result[0];
                    counters[2] += result[1];
                    chunk.clear();
                    updateProgress(jobId, counters[0], counters[1], counters[2]);
                }
            });

            xmlReader.setContentHandler(handler);
            InputStream sheet = reader.getSheetsData().next();
            xmlReader.parse(new InputSource(sheet));

            // Flush remaining rows
            if (!chunk.isEmpty()) {
                int[] result = persistChunk(chunk);
                counters[1] += result[0];
                counters[2] += result[1];
            }

            markCompleted(jobId, counters[0], counters[1], counters[2]);

        } catch (Exception e) {
            log.error("Bulk upload job {} failed: {}", jobId, e.getMessage(), e);
            markFailed(jobId, counters[0], counters[1], counters[2], e.getMessage());
        }
    }

    /**
     * Persists one chunk with duplicate detection.
     * Fetches all existing NICs in the chunk with ONE query, then filters out duplicates.
     * Returns [successCount, failCount].
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int[] persistChunk(List<String[]> rows) {
        List<String> nicsInChunk = new ArrayList<>();
        List<Customer> toInsert = new ArrayList<>();

        for (String[] row : rows) {
            if (row.length < 3) continue;
            String name = safeGet(row, 0);
            String dob = safeGet(row, 1);
            String nic = safeGet(row, 2);
            if (name.isEmpty() || dob.isEmpty() || nic.isEmpty()) continue;
            nicsInChunk.add(nic);
        }

        // Single DB query to find all already-existing NICs in this chunk
        Set<String> existingNics = new HashSet<>(customerRepository.findExistingNics(nicsInChunk));

        int success = 0, failed = 0;
        for (String[] row : rows) {
            try {
                String name = safeGet(row, 0);
                String dob = safeGet(row, 1);
                String nic = safeGet(row, 2);

                if (name.isEmpty() || dob.isEmpty() || nic.isEmpty()) {
                    failed++;
                    continue;
                }
                if (existingNics.contains(nic)) {
                    // Update existing customer — upsert behaviour
                    customerRepository.findByNicNumber(nic).ifPresent(c -> {
                        c.setName(name);
                        c.setDateOfBirth(java.time.LocalDate.parse(dob));
                        customerRepository.save(c);
                    });
                    success++;
                    continue;
                }

                Customer customer = new Customer();
                customer.setName(name);
                customer.setDateOfBirth(java.time.LocalDate.parse(dob));
                customer.setNicNumber(nic);
                toInsert.add(customer);
                success++;

            } catch (Exception e) {
                log.warn("Failed to parse row: {}", e.getMessage());
                failed++;
            }
        }

        // Batch insert the new customers
        customerRepository.saveAll(toInsert);
        return new int[]{success, failed};
    }

    // -------------------------------------------------------------------------
    // Job state helpers — each runs in its own short transaction
    // -------------------------------------------------------------------------

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(Long jobId, int total, int processed, int failed) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setTotalRecords(total);
            job.setProcessedRecords(processed);
            job.setFailedRecords(failed);
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(Long jobId, int total, int processed, int failed) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(BulkUploadJob.Status.COMPLETED);
            job.setTotalRecords(total);
            job.setProcessedRecords(processed);
            job.setFailedRecords(failed);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long jobId, int total, int processed, int failed, String error) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(BulkUploadJob.Status.FAILED);
            job.setTotalRecords(total);
            job.setProcessedRecords(processed);
            job.setFailedRecords(failed);
            job.setErrorMessage(error != null && error.length() > 2000
                ? error.substring(0, 2000) : error);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }

    // -------------------------------------------------------------------------
    // Validation & helpers
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Only Excel files (.xlsx, .xls) are accepted");
        }
    }

    private String safeGet(String[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) return "";
        return row[index].trim();
    }
}