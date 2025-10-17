package com.stockviewer.stockapi.user.auth.dto;

import com.stockviewer.stockapi.user.dto.UserDetailsDTO;

public record LoginResponse(String message, String accessToken, String tokenType, UserDetailsDTO userDetails) {}