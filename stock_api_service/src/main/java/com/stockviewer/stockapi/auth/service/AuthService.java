package com.stockviewer.stockapi.auth.service;

import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.user.entity.Role;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.exception.CredentialsTakenException;
import com.stockviewer.stockapi.user.mapper.UserMapper;
import com.stockviewer.stockapi.user.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.stockviewer.stockapi.utility.jwt.JwtUtils;
import org.springframework.stereotype.Service;
import com.stockviewer.stockapi.user.repository.UserRepository;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserMapper userMapper, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager){
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public User register(UserDTO userDTO) throws CredentialsTakenException, ResourceNotFoundException {

        if(userRepository.existsByEmail(userDTO.getEmail())) throw new CredentialsTakenException("Email address is already taken");

        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));

        user.getRoles().add(userRole);
        return userRepository.save(user);
        // TODO: account email activation
    }

    public String login(UserDTO userDTO){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDTO.getEmail(),
                        userDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtils.generateToken(authentication);
    }
}
