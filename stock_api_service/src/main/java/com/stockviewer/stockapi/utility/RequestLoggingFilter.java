package com.stockviewer.stockapi.utility;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.logging.Logger;

public class RequestLoggingFilter implements Filter {

    private static final Logger logger = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String url = httpRequest.getRequestURL().toString();
        String clientIp = HttpHelper.getClientIP(httpRequest);

        logger.info(String.format("Incoming request from IP: %s to URL: %s", clientIp, url));

        chain.doFilter(request, response);
    }
}

