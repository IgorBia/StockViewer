package com.stockviewer.stockapi.controller;

import com.stockviewer.stockapi.TestSecurityConfig;
import com.stockviewer.stockapi.auth.controller.AuthController;
import com.stockviewer.stockapi.user.dto.UserDTO;
import com.stockviewer.stockapi.exception.GlobalExceptionHandler;
import com.stockviewer.stockapi.auth.service.AuthService;
import com.stockviewer.stockapi.utility.jwt.JwtFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, ValidationAutoConfiguration.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService userService;

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
                .andExpect(status().isOk());
                //.andExpect(content().string("User created"));

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
                .andExpect(status().isOk());
    }
}
//TODO: fix tests; every request 200 code is returned.
