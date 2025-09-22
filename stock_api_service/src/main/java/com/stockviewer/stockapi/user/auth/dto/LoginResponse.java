package com.stockviewer.stockapi.user.auth.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private String message;
    private String accessToken;
    private String tokenType = "Bearer";

    public LoginResponse(String message, String accessToken) {
        this.accessToken = accessToken;
        this.message = message;
    }
    public LoginResponse(String message){
        this.message = message;
    }

}