package com.stockviewer.stockapi.config;

import lombok.Getter;

@Getter
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }

}