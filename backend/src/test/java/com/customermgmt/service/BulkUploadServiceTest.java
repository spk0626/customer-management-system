package com.customermgmt.service;

import com.customermgmt.dto.response.BulkJobResponse;
import com.customermgmt.entity.BulkUploadJob;
import com.customermgmt.entity.Customer;
import com.customermgmt.exception.ResourceNotFoundException;
import com.customermgmt.mapper.BulkUploadJobMapper;
import com.customermgmt.repository.BulkUploadJobRepository;
import com.customermgmt.repository.CustomerRepository;
import com.customermgmt.service.impl.BulkUploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkUploadService unit tests")
class BulkUploadServiceTest {

    @Mock private BulkUploadJobRepository jobRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private BulkUploadJobMapper jobMapper;

    // Use the concrete impl — @InjectMocks needs a concrete class, not an interface
    @InjectMocks
    private BulkUploadServiceImpl bulkUploadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bulkUploadService, "chunkSize", 50);
    }

    // ------------------------------------------------------------------
    // submitBulkUpload — file validation
    // ------------------------------------------------------------------

    @Test
    @DisplayName("submitBulkUpload — rejects non-Excel files")
    void submitBulkUpload_invalidFileType_throws() {
        MockMultipartFile csvFile = new MockMultipartFile(
            "file", "data.csv", "text/csv", "name,dob,nic".getBytes()
        );

        assertThatThrownBy(() -> bulkUploadService.submitBulkUpload(csvFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Only Excel files");
    }

    @Test
    @DisplayName("submitBulkUpload — rejects empty file")
    void submitBulkUpload_emptyFile_throws() {
        MockMultipartFile empty = new MockMultipartFile(
            "file", "empty.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]
        );

        assertThatThrownBy(() -> bulkUploadService.submitBulkUpload(empty))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("empty");
    }

    @Test
    @DisplayName("submitBulkUpload — creates PENDING job record and returns response")
    void submitBulkUpload_validFile_createsPendingJob() {
        // Non-empty xlsx bytes — just needs to pass the "not empty" check.
        // Actual SAX parsing is tested separately; here we test the job-creation path.
        MockMultipartFile file = new MockMultipartFile(
            "file", "customers.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[]{0x50, 0x4B, 0x03, 0x04} // PK ZIP magic bytes
        );

        BulkUploadJob savedJob = new BulkUploadJob();
        savedJob.setId(1L);
        savedJob.setFileName("customers.xlsx");
        savedJob.setStatus(BulkUploadJob.Status.PENDING);

        BulkJobResponse expectedResponse = BulkJobResponse.builder()
            .jobId(1L).fileName("customers.xlsx")
            .status(BulkUploadJob.Status.PENDING)
            .processedRecords(0).failedRecords(0)
            .build();

        when(jobRepository.save(any(BulkUploadJob.class))).thenReturn(savedJob);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(savedJob));
        when(jobMapper.toResponse(savedJob)).thenReturn(expectedResponse);

        BulkJobResponse result = bulkUploadService.submitBulkUpload(file);

        assertThat(result.getJobId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BulkUploadJob.Status.PENDING);
        // Job must be persisted before async processing begins
        verify(jobRepository).save(any(BulkUploadJob.class));
    }

    // ------------------------------------------------------------------
    // persistChunk — row-level processing logic
    // ------------------------------------------------------------------

    @Test
    @DisplayName("persistChunk — skips rows with missing mandatory fields")
    void persistChunk_missingFields_countedAsFailed() {
        when(customerRepository.findExistingNics(any())).thenReturn(Collections.emptyList());
        when(customerRepository.saveAll(any())).thenReturn(Collections.emptyList());

        int[] result = bulkUploadService.persistChunk(Arrays.asList(
            new String[]{"Kamal Perera", "1990-05-15", ""},           // empty NIC  -> failed
            new String[]{"", "1985-11-22", "851111111V"},              // empty name -> failed
            new String[]{"Nimal Silva", "1985-11-22", "851234567V"}    // valid       -> success
        ));

        assertThat(result[0]).isEqualTo(1); // 1 success
        assertThat(result[1]).isEqualTo(2); // 2 failed
    }

    @Test
    @DisplayName("persistChunk — upserts when NIC already exists in database")
    void persistChunk_existingNic_updatesRecord() {
        when(customerRepository.findExistingNics(any()))
            .thenReturn(Collections.singletonList("901234567V"));
        when(customerRepository.saveAll(any())).thenReturn(Collections.emptyList());

        Customer existing = new Customer();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setNicNumber("901234567V");
        existing.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        when(customerRepository.findByNicNumber("901234567V"))
            .thenReturn(Optional.of(existing));
        when(customerRepository.save(any())).thenReturn(existing);

        int[] result = bulkUploadService.persistChunk(Collections.singletonList(
            new String[]{"Updated Name", "1990-05-15", "901234567V"}
        ));

        assertThat(result[0]).isEqualTo(1); // counted as success (upsert)
        verify(customerRepository).save(argThat(c -> "Updated Name".equals(c.getName())));
    }

    @Test
    @DisplayName("persistChunk — batch-inserts new customers as a group (not one-by-one)")
    void persistChunk_newCustomers_batchInserted() {
        when(customerRepository.findExistingNics(any())).thenReturn(Collections.emptyList());
        when(customerRepository.saveAll(any())).thenReturn(Collections.emptyList());

        bulkUploadService.persistChunk(Arrays.asList(
            new String[]{"Kamal Perera",  "1990-05-15", "901111111V"},
            new String[]{"Nimal Silva",   "1985-11-22", "851111111V"},
            new String[]{"Priya Fernando","1992-03-08", "921111111V"}
        ));

        // saveAll must be called once (batch), not three separate save() calls
        verify(customerRepository, times(1)).saveAll(any());
        verify(customerRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // getJobStatus
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getJobStatus — returns mapped response when job exists")
    void getJobStatus_found_returnsResponse() {
        BulkUploadJob job = new BulkUploadJob();
        job.setId(1L);
        job.setStatus(BulkUploadJob.Status.PROCESSING);

        BulkJobResponse response = BulkJobResponse.builder()
            .jobId(1L).status(BulkUploadJob.Status.PROCESSING).build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobMapper.toResponse(job)).thenReturn(response);

        BulkJobResponse result = bulkUploadService.getJobStatus(1L);

        assertThat(result.getStatus()).isEqualTo(BulkUploadJob.Status.PROCESSING);
    }

    @Test
    @DisplayName("getJobStatus — throws ResourceNotFoundException when job not found")
    void getJobStatus_notFound_throws() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bulkUploadService.getJobStatus(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");
    }
}