package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.ForgotPasswordRequestDto;
import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.ResetPasswordRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.UserRegistrationEvent;
import com.fundoo.notes.exception.DuplicateEmailException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.TokenService;
import com.fundoo.notes.service.UserRegistrationEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordService passwordService;
    private TokenService tokenService;
    private UserRegistrationEventPublisher eventPublisher;
    private OtpService otpService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordService.class);
        tokenService = mock(TokenService.class);
        eventPublisher = mock(UserRegistrationEventPublisher.class);
        otpService = mock(OtpService.class);
        userService = new UserServiceImpl(userRepository, passwordService, tokenService, eventPublisher, otpService);
        when(passwordService.encode(any())).thenAnswer(invocation -> "hashed-" + invocation.getArgument(0));
        when(passwordService.matches(any(), any())).thenAnswer(invocation ->
                ("hashed-" + invocation.getArgument(0)).equals(invocation.getArgument(1)));
    }

    private User storedUser() {
        User user = new User("Navya", "Raj", "navya@example.com", "hashed-secret123");
        user.setId(1L);
        return user;
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByEmail("navya@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponseDto response = userService.register(new UserRegisterRequestDto("Navya", "Raj", "Navya@Example.com", "secret123"));

        assertEquals(1L, response.getId());
        assertEquals("navya@example.com", response.getEmail());
    }

    @Test
    void testRegister_StoresEmailInLowerCase() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        userService.register(new UserRegisterRequestDto("Navya", "Raj", " NAVYA@EXAMPLE.COM ", "secret123"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("navya@example.com", captor.getValue().getEmail());
    }

    @Test
    void testRegister_StoresEncodedPassword() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-secret123", captor.getValue().getPassword());
    }

    @Test
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByEmail("navya@example.com")).thenReturn(true);
        assertThrows(DuplicateEmailException.class,
                () -> userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123")));
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(storedUser()));
        when(tokenService.generateToken(any(User.class))).thenReturn("jwt-token");
        LoginResponseDto response = userService.login(new LoginRequestDto("navya@example.com", "secret123"));
        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void testLogin_UnknownEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> userService.login(new LoginRequestDto("x@example.com", "secret123")));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(storedUser()));
        assertThrows(InvalidCredentialsException.class, () -> userService.login(new LoginRequestDto("navya@example.com", "wrong123")));
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void testRegister_WritesLogs(CapturedOutput output) {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });
        userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123"));
        assertTrue(output.getOut().contains("Registration request received for email: navya@example.com"));
        assertTrue(output.getOut().contains("User registered successfully with id: 11"));
        // the raw password must never be logged
        assertFalse(output.getOut().contains("secret123"));
    }

    @Test
    void testRegister_PublishesRegistrationEvent() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        userService.register(new UserRegisterRequestDto("Navya", "Raj", "navya@example.com", "secret123"));
        ArgumentCaptor<UserRegistrationEvent> captor = ArgumentCaptor.forClass(UserRegistrationEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertEquals(3L, captor.getValue().getUserId());
        assertEquals("navya@example.com", captor.getValue().getEmail());
    }

    @Test
    void testForgotPassword_GeneratesOtp() {
        when(userRepository.existsByEmail("navya@example.com")).thenReturn(true);
        when(otpService.generateOtp("navya@example.com")).thenReturn("123456");
        assertEquals("123456", userService.forgotPassword(new ForgotPasswordRequestDto("Navya@Example.com")).getOtp());
    }

    @Test
    void testForgotPassword_UnknownEmail() {
        assertThrows(InvalidCredentialsException.class, () -> userService.forgotPassword(new ForgotPasswordRequestDto("x@example.com")));
        verifyNoInteractions(otpService);
    }

    @Test
    void testResetPassword_Success() {
        User user = storedUser();
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyOtp("navya@example.com", "123456")).thenReturn(true);
        userService.resetPassword(new ResetPasswordRequestDto("navya@example.com", "123456", "newpass123"));
        assertEquals("hashed-newpass123", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testResetPassword_WrongOtp() {
        when(userRepository.findByEmail("navya@example.com")).thenReturn(Optional.of(storedUser()));
        when(otpService.verifyOtp("navya@example.com", "000000")).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword(new ResetPasswordRequestDto("navya@example.com", "000000", "newpass123")));
        verify(userRepository, never()).save(any());
    }
}
