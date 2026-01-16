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
import com.stockviewer.stockapi.candle.entity.Pair;
import com.stockviewer.stockapi.exception.ResourceNotFoundException;
import com.stockviewer.stockapi.candle.service.CandleService;
import java.util.List;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CandleService candleService;
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, UserMapper userMapper, CandleService candleService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.candleService = candleService;
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User setManagedAsset(int riskTolerance) {
        Pair pair = candleService.getPairbyRiskTolerance(riskTolerance);
        if(pair == null) {
            throw new ResourceNotFoundException("Pair not found for symbol: " + riskTolerance);
        }
        User user = getUserFromContext();
        user.getWallets().forEach(wallet -> wallet.setManagedAsset(pair.getBaseAsset()));
        logger.info("Set managed asset to {} for user {}", pair.getBaseAsset().getSymbol(), user.getEmail());
        return userRepository.save(user);
    }

    public List<User> getUsersWithManagedAssetsInPair(Pair pair) {
        return userRepository.findAllByWalletsManagedAsset(pair.getBaseAsset());
    }
}
