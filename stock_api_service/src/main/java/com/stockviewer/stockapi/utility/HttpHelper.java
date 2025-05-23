package com.stockviewer.stockapi.utility;

import jakarta.servlet.http.HttpServletRequest;

public class HttpHelper {
    public static String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            String ip = request.getRemoteAddr();
            if (ip == null) {
                throw new IllegalStateException("IP is null");
            }
            return ip;
        }
        return xfHeader.split(",")[0].trim();
    }
}
