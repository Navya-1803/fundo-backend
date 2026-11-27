package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.ForgotPasswordRequestDto;
import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.ResetPasswordRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.PasswordResetResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.service.UserService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for user registration and login. Login returns a JWT
 * which the client sends in the Authorization header of later requests.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterRequestDto requestDto) {
        log.debug("POST /api/users/register");
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(requestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.debug("POST /api/users/login");
        return ResponseEntity.ok(userService.login(requestDto));
    }

    // sends a one time password to the user's email address
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponseDto> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto requestDto) {
        log.debug("POST /api/users/forgot-password");
        return ResponseEntity.ok(userService.forgotPassword(requestDto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponseDto> resetPassword(@Valid @RequestBody ResetPasswordRequestDto requestDto) {
        log.debug("POST /api/users/reset-password");
        return ResponseEntity.ok(userService.resetPassword(requestDto));
    }
}
