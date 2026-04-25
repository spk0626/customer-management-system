package com.customermgmt.dto.response;

import com.customermgmt.entity.BulkUploadJob;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulkJobResponse {
    private Long jobId;
    private String fileName;
    private BulkUploadJob.Status status;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer failedRecords;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private Double progressPercentage;
}
