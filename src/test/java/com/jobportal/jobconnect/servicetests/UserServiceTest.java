package com.jobportal.jobconnect.servicetests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jobportal.jobconnect.dto.*;
import com.jobportal.jobconnect.model.Role;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.repository.UserRepository;
import com.jobportal.jobconnect.service.TwilioSMSService;
import com.jobportal.jobconnect.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TwilioSMSService smsService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("12345");
        user.setMobileNumber("9876543210");
        user.setUserType(Role.valueOf("EMPLOYER"));

    }

    // ✅ Test: Invalid password login
    @Test
    void testLoginUser_InvalidPassword() {
        UserLoginDTO dto = new UserLoginDTO("john@example.com", "wrong");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.loginUser(dto));
    }

    // ✅ Test: Get user by email success
    @Test
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserByEmail("john@example.com");

        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
    }

    // ✅ Test: Get user by email - not found
    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByEmail("unknown@example.com"));
    }

    // ✅ Test: Validate user success
    @Test
    void testValidateUser_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        User validated = userService.validateUser("john@example.com", "12345");

        assertNotNull(validated);
        assertEquals("John Doe", validated.getFullName());
    }

    // ✅ Test: Validate user wrong password
    @Test
    void testValidateUser_WrongPassword() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.validateUser("john@example.com", "wrong"));
    }

    // ✅ Test: Find by email
    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByEmail("john@example.com");

        assertTrue(found.isPresent());
    }

    // ✅ Test: Save user
    @Test
    void testSaveUser() {
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.saveUser(user);

        assertEquals("John Doe", saved.getFullName());
        verify(userRepository).save(user);
    }

    // ✅ Test: Get user by ID
    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}
