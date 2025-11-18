package com.stockviewer.stockapi.user.auth.service;

import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.user.entity.Role;
import com.stockviewer.stockapi.user.entity.User;
import com.stockviewer.stockapi.user.mapper.UserMapper;
import com.stockviewer.stockapi.user.repository.RoleRepository;
import com.stockviewer.stockapi.user.auth.jwt.JwtUtils;
import com.stockviewer.stockapi.user.repository.UserRepository;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;
import com.stockviewer.stockapi.exception.CredentialsTakenException;

import com.stockviewer.stockapi.wallet.service.WalletService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final WalletService walletService;

    public AuthService(UserMapper userMapper, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager, WalletService walletService){
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.walletService = walletService;
    }

    public void register(UserDTO userDTO) throws CredentialsTakenException, ResourceNotFoundException {

        if(userRepository.existsByEmail(userDTO.getEmail())) throw new CredentialsTakenException("Email address is already taken");

        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));

        user.getRoles().add(userRole);
        userRepository.save(user);

        walletService.createWalletForUser(user);

        // log the user information e.g. wallet data, email etc.
        System.out.println("User registered: " + user.getEmail());
        System.out.println("User wallets: " + user.getWallets());
        System.out.println("User assets: " + user.getWallets().stream()
                .flatMap(wallet -> wallet.getOwnedAssets().stream())
                .toList());

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
