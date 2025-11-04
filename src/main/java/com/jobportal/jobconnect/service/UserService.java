package com.jobportal.jobconnect.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jobportal.jobconnect.dto.UserLoginDTO;
import com.jobportal.jobconnect.dto.UserResponseDTO;
import com.jobportal.jobconnect.dto.UserSignupDTO;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private TwilioSMSService smsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    /**
     * Registers a new user after validating uniqueness of email.
     * Encrypts password and sends SMS confirmation.
     */
    public UserResponseDTO registerUser(UserSignupDTO dto) {

        // Check if email already exists
        Optional<User> existingUser = repo.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(dto.getName());
        user.setEmail(dto.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 🔒 Secure password
        user.setMobileNumber(dto.getPhone());
        user.setUserType(dto.getRole());
        repo.save(user);

        // ✅ Try sending SMS, but don't break registration if Twilio fails
        try {
            notificationService.sendRegistrationSuccess(user);
        } catch (Exception e) {
            System.err.println("⚠️ SMS sending failed: " + e.getMessage());
        }

        // ✅ Return success even if SMS fails
        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getUserType());
    }

    /**
     * Validates login credentials and returns success message.
     */
    public String loginUser(UserLoginDTO dto) {
        User user = repo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Email or Password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Email or Password");
        }

        return "Login successful";
    }

    /**
     * Fetches user details by email (used for session setup after login).
     */
    public UserResponseDTO getUserByEmail(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponseDTO(user.getId(), user.getFullName(), user.getEmail(), user.getMobileNumber(),
                user.getUserType());
    }

    /**
     * Validates a user's credentials (email and password).
     * <p>
     * This method checks whether a user exists for the given email and verifies if
     * the provided password matches the stored one. If either the email does not
     * exist
     * or the password is incorrect, it throws a {@link RuntimeException}.
     * </p>
     *
     * @param email    the user's email address
     * @param password the user's plain text password
     * @return the {@link User} object if validation is successful
     * @throws RuntimeException if the email or password is invalid
     */
    public User validateUser(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid Email or Password"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid Email or Password");
        }

        return user;
    }

    /**
     * ✅ Finds user entity by email (used internally in job seeker controller).
     */
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    // save user
    public User saveUser(User user) {
        return repo.save(user);
    }

    /**
     * ✅ Fetches user by ID.
     */
    public User getUserById(Long id) {
        return repo.findById(id).orElse(null);
    }

}
