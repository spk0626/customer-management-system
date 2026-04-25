package com.customermgmt.controller;

import com.customermgmt.dto.response.ApiResponse;
import com.customermgmt.dto.response.BulkJobResponse;
import com.customermgmt.service.BulkUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/bulk")
@RequiredArgsConstructor
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;

    /**
     * POST /api/v1/bulk/upload
     * Accepts multipart Excel file, returns job ID immediately.
     * Processing runs asynchronously — poll /api/v1/bulk/jobs/{jobId} for progress.
     *
     * Expected Excel columns: Name | DateOfBirth (yyyy-MM-dd) | NIC
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<BulkJobResponse>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        BulkJobResponse job = bulkUploadService.submitBulkUpload(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.ok("File accepted. Processing started.", job));
    }

    /**
     * GET /api/v1/bulk/jobs/{jobId}
     * Poll this endpoint to track processing progress.
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<BulkJobResponse>> getJobStatus(
            @PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.ok(bulkUploadService.getJobStatus(jobId)));
    }
}
