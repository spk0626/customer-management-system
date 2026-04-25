package com.customermgmt.service;

import com.customermgmt.dto.response.BulkJobResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BulkUploadService {

    /**
     * Accepts the uploaded file, creates a job record, and kicks off async processing.
     * Returns immediately with the job ID so the frontend can poll for progress.
     */
    BulkJobResponse submitBulkUpload(MultipartFile file);

    BulkJobResponse getJobStatus(Long jobId);
}
