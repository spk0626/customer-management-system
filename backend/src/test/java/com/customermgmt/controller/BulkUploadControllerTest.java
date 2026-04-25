package com.customermgmt.controller;

import com.customermgmt.dto.response.ApiResponse;
import com.customermgmt.dto.response.BulkJobResponse;
import com.customermgmt.entity.BulkUploadJob;
import com.customermgmt.exception.ResourceNotFoundException;
import com.customermgmt.service.BulkUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BulkUploadController.class)
@DisplayName("BulkUploadController MockMvc tests")
class BulkUploadControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BulkUploadService bulkUploadService;

    @Test
    @DisplayName("POST /api/v1/bulk/upload — 202 Accepted on valid file")
    void upload_validFile_returns202() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "data.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "fake-xlsx-content".getBytes()
        );

        BulkJobResponse jobResponse = BulkJobResponse.builder()
            .jobId(1L).fileName("data.xlsx")
            .status(BulkUploadJob.Status.PENDING)
            .processedRecords(0).failedRecords(0)
            .build();

        when(bulkUploadService.submitBulkUpload(any())).thenReturn(jobResponse);

        mockMvc.perform(multipart("/api/v1/bulk/upload").file(file))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.jobId").value(1))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/bulk/jobs/{id} — 200 with progress data")
    void getJobStatus_found_returns200() throws Exception {
        BulkJobResponse jobResponse = BulkJobResponse.builder()
            .jobId(1L).fileName("data.xlsx")
            .status(BulkUploadJob.Status.PROCESSING)
            .totalRecords(1000).processedRecords(500).failedRecords(2)
            .progressPercentage(50.0)
            .build();

        when(bulkUploadService.getJobStatus(1L)).thenReturn(jobResponse);

        mockMvc.perform(get("/api/v1/bulk/jobs/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PROCESSING"))
            .andExpect(jsonPath("$.data.progressPercentage").value(50.0));
    }

    @Test
    @DisplayName("GET /api/v1/bulk/jobs/{id} — 404 when job not found")
    void getJobStatus_notFound_returns404() throws Exception {
        when(bulkUploadService.getJobStatus(99L))
            .thenThrow(new ResourceNotFoundException("BulkUploadJob", 99L));

        mockMvc.perform(get("/api/v1/bulk/jobs/99"))
            .andExpect(status().isNotFound());
    }
}