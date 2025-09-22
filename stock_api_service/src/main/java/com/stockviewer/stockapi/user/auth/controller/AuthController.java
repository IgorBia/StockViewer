package com.stockviewer.stockapi.user.auth.controller;

import ch.qos.logback.classic.Logger;
import com.stockviewer.stockapi.exception.CredentialsTakenException;
import com.stockviewer.stockapi.user.auth.dto.LoginResponse;
import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.user.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/v1/users")
public class AuthController {

    private final AuthService authService;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserDTO userDTO){
        try{
            authService.register(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created");
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (CredentialsTakenException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody UserDTO userDTO) {
        try{
            String token = authService.login(userDTO);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new LoginResponse("Logged in successfully", token));
        } catch(BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Incorrect email address or password"));
        }
    }
}