package com.stockviewer.stockapi.utility;

import lombok.Getter;

@Getter
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }

}