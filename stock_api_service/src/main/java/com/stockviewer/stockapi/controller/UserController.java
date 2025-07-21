package com.stockviewer.stockapi.controller;

import ch.qos.logback.classic.Logger;
import com.stockviewer.stockapi.utility.LoginResponse;
import com.stockviewer.stockapi.dto.UserDTO;
import com.stockviewer.stockapi.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO){
        try{
            userService.register(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created");
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(e.getMessage());
        }
        // TODO: Handling repeated email, wrong password etc..
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody UserDTO userDTO) {
        try{
            String token = userService.login(userDTO);
        } catch(BadCredentialsException e){
            return ResponseEntity.status(403).body("Incorrect email or password");
        }
        return ResponseEntity.ok(new LoginResponse(token));
    }
}