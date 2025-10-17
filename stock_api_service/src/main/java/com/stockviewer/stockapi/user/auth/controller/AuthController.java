package com.stockviewer.stockapi.user.auth.controller;

import ch.qos.logback.classic.Logger;
import com.stockviewer.stockapi.exception.CredentialsTakenException;
import com.stockviewer.stockapi.user.auth.dto.LoginResponse;
import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.user.auth.service.AuthService;
import com.stockviewer.stockapi.user.dto.UserDetailsDTO;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;
import java.util.HashMap;

@RestController
@RequestMapping("users")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<HashMap<String, String>> register(@Valid @RequestBody UserDTO userDTO){
        authService.register(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new HashMap<String, String>() {{
            put("message", "User registered successfully");
        }});
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody UserDTO userDTO) {
        try{
            String token = authService.login(userDTO);
            UserDetailsDTO userDTOResponse = userService.getUserDetailsDTOByEmail(userDTO.getEmail());
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(new LoginResponse("Logged in successfully", token, "Bearer", userDTOResponse));
        } catch(BadCredentialsException e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Incorrect email address or password", null, null, null));
        }
    }
}