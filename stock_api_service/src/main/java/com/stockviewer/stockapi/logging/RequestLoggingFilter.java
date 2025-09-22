package com.stockviewer.stockapi.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String url = httpRequest.getRequestURL().toString();
        String clientIp = HttpHelper.getClientIP(httpRequest);

        logger.info("Incoming request from IP: {} to URL: {}", clientIp, url);

        chain.doFilter(request, response);
    }
}

