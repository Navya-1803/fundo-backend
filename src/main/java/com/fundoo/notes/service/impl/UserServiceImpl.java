package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.ForgotPasswordRequestDto;
import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.ResetPasswordRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.PasswordResetResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.UserRegistrationEvent;
import com.fundoo.notes.exception.DuplicateEmailException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.EmailService;
import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.TokenService;
import com.fundoo.notes.service.UserRegistrationEventPublisher;
import com.fundoo.notes.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registers users with a hashed password and issues a JWT on login.
 *
 * @author Navya Raj
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final UserRegistrationEventPublisher eventPublisher;
    private final OtpService otpService;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, PasswordService passwordService, TokenService tokenService,
                           UserRegistrationEventPublisher eventPublisher, OtpService otpService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Override
    public UserResponseDto register(UserRegisterRequestDto requestDto) {
        // emails are stored in lower case so the same address cannot register twice
        String email = requestDto.getEmail().trim().toLowerCase();
        log.info("Registration request received for email: {}", email);
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected, email already registered: {}", email);
            throw new DuplicateEmailException("Email already registered: " + email);
        }
        // only the BCrypt hash of the password is stored
        String encodedPassword = passwordService.encode(requestDto.getPassword());
        User user = new User(requestDto.getFirstName().trim(), requestDto.getLastName(), email, encodedPassword);
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());
        // the welcome email is sent asynchronously through JMS
        eventPublisher.publish(new UserRegistrationEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName()));
        return mapToUserResponse(savedUser);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        String email = requestDto.getEmail().trim().toLowerCase();
        log.info("Login attempt for email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Login failed, no account for email: {}", email);
            return new InvalidCredentialsException("Invalid email or password");
        });
        if (!passwordService.matches(requestDto.getPassword(), user.getPassword())) {
            log.warn("Login failed, wrong password for user id: {}", user.getId());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String token = tokenService.generateToken(user);
        log.info("Login successful for user id: {}", user.getId());
        return new LoginResponseDto(token, "Login successful");
    }

    @Override
    public PasswordResetResponseDto forgotPassword(ForgotPasswordRequestDto requestDto) {
        String email = requestDto.getEmail().trim().toLowerCase();
        log.info("Password reset requested for {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Password reset requested for unknown email: {}", email);
            return new InvalidCredentialsException("No account found with this email");
        });
        // the OTP is stored in Redis, expires automatically and is sent to the user's email
        String otp = otpService.generateOtp(email);
        long validMinutes = otpService.getValidityMinutes();
        emailService.sendOtpEmail(email, user.getFirstName(), otp, validMinutes);
        return new PasswordResetResponseDto("OTP sent to " + email + ", it is valid for " + validMinutes + " minutes");
    }

    @Override
    public PasswordResetResponseDto resetPassword(ResetPasswordRequestDto requestDto) {
        String email = requestDto.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("No account found with this email"));
        if (!otpService.verifyOtp(email, requestDto.getOtp())) {
            log.warn("Password reset failed, wrong or expired OTP for user id: {}", user.getId());
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        user.setPassword(passwordService.encode(requestDto.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset for user id: {}", user.getId());
        return new PasswordResetResponseDto("Password changed successfully, please sign in");
    }

    // converts the entity into the response sent to the client
    private UserResponseDto mapToUserResponse(User user) {
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getCreatedAt());
    }
}
