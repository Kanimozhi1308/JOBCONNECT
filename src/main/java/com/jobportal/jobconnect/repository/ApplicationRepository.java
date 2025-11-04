package com.jobportal.jobconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.jobconnect.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByUserIdAndJobId(Long userId, Long jobId);

    List<Application> findByUserId(Long userId);

    List<Application> findByApplicantEmail(String email);

    List<Application> findByJobIdIn(List<Long> jobIds);

    List<Application> findByJobId(Long jobId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Application a WHERE a.jobId = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);

}
