package com.jobportal.jobconnect.repositorytests;

import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {

    @Test
    public void testFindByEmail() {
        UserRepository repo = mock(UserRepository.class);

        User user = new User();
        user.setId(1L);
        user.setFullName("Kanimozhi");
        user.setEmail("kani@example.com");

        when(repo.findByEmail("kani@example.com")).thenReturn(Optional.of(user));

        Optional<User> foundUser = repo.findByEmail("kani@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("kani@example.com", foundUser.get().getEmail());
        assertEquals("Kanimozhi", foundUser.get().getFullName());
    }

    @Test
    public void testFindByEmail_NotFound() {
        UserRepository repo = mock(UserRepository.class);

        when(repo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = repo.findByEmail("unknown@example.com");

        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testExistsByEmail_True() {
        UserRepository repo = mock(UserRepository.class);

        when(repo.existsByEmail("kani@example.com")).thenReturn(true);

        boolean exists = repo.existsByEmail("kani@example.com");

        assertTrue(exists);
    }

    @Test
    public void testExistsByEmail_False() {
        UserRepository repo = mock(UserRepository.class);

        when(repo.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean exists = repo.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }
}
