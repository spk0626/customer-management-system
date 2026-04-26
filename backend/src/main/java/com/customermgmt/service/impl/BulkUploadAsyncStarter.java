package com.customermgmt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BulkUploadAsyncStarter {

    private final ObjectProvider<BulkUploadServiceImpl> bulkUploadServiceProvider;

    @Async("bulkTaskExecutor")
    public void startProcessing(Long jobId, String tempFilePath) {
        bulkUploadServiceProvider.getObject().processAsync(jobId, tempFilePath);
    }
}
