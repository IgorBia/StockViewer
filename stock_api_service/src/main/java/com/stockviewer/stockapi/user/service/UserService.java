package com.stockviewer.stockapi.user.service;

import com.stockviewer.stockapi.user.auth.dto.CustomUserDetails;
import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.user.dto.UserDetailsDTO;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.user.mapper.UserMapper;
import com.stockviewer.stockapi.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public User getUserById(UUID userId) {
        return userRepository.findByUserId(userId);
    }

    public User getUserFromContext(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return getUserById(principal.getId());
    }

    public UserDetailsDTO getUserDetailsDTOByEmail(String email) {
        User user = getUserByEmail(email);
        return userMapper.toDTO(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }
}
