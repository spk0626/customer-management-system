package com.customermgmt.mapper;

import com.customermgmt.dto.response.BulkJobResponse;
import com.customermgmt.entity.BulkUploadJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BulkUploadJobMapper {

    @Mapping(target = "jobId", source = "id")
    @Mapping(target = "progressPercentage", expression =
        "java(job.getTotalRecords() != null && job.getTotalRecords() > 0 " +
        "? (double) job.getProcessedRecords() / job.getTotalRecords() * 100 : 0.0)")
    BulkJobResponse toResponse(BulkUploadJob job);
}
