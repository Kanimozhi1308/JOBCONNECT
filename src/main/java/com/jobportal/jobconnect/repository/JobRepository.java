package com.jobportal.jobconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobportal.jobconnect.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByTitleContainingIgnoreCase(String keyword);

    List<Job> findByLocationContainingIgnoreCase(String location);

    List<Job> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(String keyword, String location);

    List<Job> findByEmployerId(Long employerId);

    Optional<Job> findByIdAndEmployerId(Long id, Long employerId);

}
