package com.stockviewer.stockapi.controller;

import ch.qos.logback.classic.Logger;
import com.stockviewer.stockapi.exception.CredentialsTakenException;
import com.stockviewer.stockapi.utility.LoginResponse;
import com.stockviewer.stockapi.dto.UserDTO;
import com.stockviewer.stockapi.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserDTO userDTO){
        try{
            userService.register(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created");
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (CredentialsTakenException e){
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody UserDTO userDTO) {
        try{
            String token = userService.login(userDTO);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new LoginResponse("Logged in successfully", token));
        } catch(BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new LoginResponse("Incorrect email address or password"));
        }
    }
}