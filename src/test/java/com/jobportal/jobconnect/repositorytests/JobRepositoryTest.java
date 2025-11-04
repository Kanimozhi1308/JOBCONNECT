package com.jobportal.jobconnect.repositorytests;

import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.repository.JobRepository;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class JobRepositoryTest {

    @Test
    public void testFindByTitleContainingIgnoreCase() {
        JobRepository repo = mock(JobRepository.class);

        Job job1 = new Job();
        job1.setTitle("Java Developer");

        Job job2 = new Job();
        job2.setTitle("Junior Java Developer");

        when(repo.findByTitleContainingIgnoreCase("java"))
                .thenReturn(Arrays.asList(job1, job2));

        List<Job> result = repo.findByTitleContainingIgnoreCase("java");

        assertEquals(2, result.size());
        assertTrue(result.get(0).getTitle().toLowerCase().contains("java"));
    }

    @Test
    public void testFindByLocationContainingIgnoreCase() {
        JobRepository repo = mock(JobRepository.class);

        Job job = new Job();
        job.setLocation("Chennai");

        when(repo.findByLocationContainingIgnoreCase("chennai"))
                .thenReturn(Collections.singletonList(job));

        List<Job> result = repo.findByLocationContainingIgnoreCase("chennai");

        assertEquals(1, result.size());
        assertEquals("Chennai", result.get(0).getLocation());
    }

    @Test
    public void testFindByTitleAndLocationContainingIgnoreCase() {
        JobRepository repo = mock(JobRepository.class);

        Job job = new Job();
        job.setTitle("Spring Boot Developer");
        job.setLocation("Bangalore");

        when(repo.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase("spring", "bangalore"))
                .thenReturn(Collections.singletonList(job));

        List<Job> result = repo.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase("spring", "bangalore");

        assertEquals(1, result.size());
        assertEquals("Spring Boot Developer", result.get(0).getTitle());
        assertEquals("Bangalore", result.get(0).getLocation());
    }

    @Test
    public void testFindByEmployerId() {
        JobRepository repo = mock(JobRepository.class);

        Job job1 = new Job();
        job1.setEmployerId(101L);

        Job job2 = new Job();
        job2.setEmployerId(101L);

        when(repo.findByEmployerId(101L)).thenReturn(Arrays.asList(job1, job2));

        List<Job> result = repo.findByEmployerId(101L);

        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getEmployerId());
    }
}
