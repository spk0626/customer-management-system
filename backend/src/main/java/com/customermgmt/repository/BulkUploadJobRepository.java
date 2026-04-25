package com.customermgmt.repository;

import com.customermgmt.entity.BulkUploadJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkUploadJobRepository extends JpaRepository<BulkUploadJob, Long> {
}
