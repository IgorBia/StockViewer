package com.stockviewer.stockapi.controller;

import com.stockviewer.stockapi.TestSecurityConfig;
import com.stockviewer.stockapi.config.SecurityConfig;
import com.stockviewer.stockapi.dto.UserDTO;
import com.stockviewer.stockapi.exception.GlobalExceptionHandler;
import com.stockviewer.stockapi.service.UserService;
import com.stockviewer.stockapi.utility.CustomUserDetailsService;
import com.stockviewer.stockapi.utility.jwt.JwtFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        String json = """
        {
            "email": "test@gmail.com",
            "password": "Password1"
        }
        """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("User created"));

        verify(userService, never()).register(any(UserDTO.class));
    }


    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        String json = """
            {
                "email": "invalidEmail",
                "password": "Password1"
            }
            """;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
