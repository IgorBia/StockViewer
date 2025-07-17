package com.stockviewer.stockapi.service;

import com.stockviewer.stockapi.dto.UserDTO;
import com.stockviewer.stockapi.entity.Role;
import com.stockviewer.stockapi.entity.User;
import com.stockviewer.stockapi.mapper.UserMapper;
import com.stockviewer.stockapi.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.stockviewer.stockapi.repository.UserRepository;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder){
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

//    public boolean login(UserDTO userDTO){
//        User user = userMapper.toEntity(userDTO);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        return userRepository.exists();
//    }
}
