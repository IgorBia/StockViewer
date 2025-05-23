package com.stockviewer.stockapi.utility;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpHelperTest {

    @Test
    @DisplayName("Regular request with proxy and valid IP")
    void ValidIP() {
        String ip = "192.168.1.1";
        String xff = ip + ", 10.0.0.1, 172.16.0.1";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        request.addHeader("X-Forwarded-For", xff);
        String clientIP = HttpHelper.getClientIP(request);
        assertEquals(ip, clientIP);
    }

    @Test
    @DisplayName("Request when client is not using a proxy")
    void ValidIPNoProxy() {
        String ip = "192.168.1.1";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        String clientIP = HttpHelper.getClientIP(request);
        assertEquals(ip, clientIP);
    }

    @Test
    @DisplayName("Request when IP is null")
    void EmptyIP() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(null);
        assertThrows(IllegalStateException.class, () -> HttpHelper.getClientIP(request));
    }

}
