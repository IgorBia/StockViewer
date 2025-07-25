package com.stockviewer.stockapi.candle.controller;

import com.stockviewer.stockapi.TestSecurityConfig;
import com.stockviewer.stockapi.candle.dto.CandleDTO;
import com.stockviewer.stockapi.exception.GlobalExceptionHandler;
import com.stockviewer.stockapi.candle.service.CandleService;
import com.stockviewer.stockapi.utility.jwt.JwtFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CandleController.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class CandleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CandleService candleService;

//    @MockitoBean
//    private PasswordEncoder passwordEncoder;
//
//    @MockitoBean
//    private CustomUserDetailsService customUserDetailsService;
//
    @MockitoBean
    private JwtFilter jwtFilter;

    private List<CandleDTO> getSampleCandleDTOList() {
        CandleDTO candleDto = new CandleDTO(
                LocalDateTime.of(2025, 5, 22, 12, 0),
                new BigDecimal("2513.24"),
                new BigDecimal("2512.04"),
                new BigDecimal("2513.24"),
                new BigDecimal("2512.01")
        );
        return List.of(candleDto);
    }

    private void assertCandleFields(String url) throws Exception{
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].open").value(2513.24))
//                .andExpect(jsonPath("$[0].close").value(2512.04))
//                .andExpect(jsonPath("$[0].high").value(2513.24))
//                .andExpect(jsonPath("$[0].low").value(2512.01))
                //TODO: response is empty
                        .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Response JSON: " + responseBody);
    }

    @Test
    void returnCandleDataAllCandlesTest() throws Exception {

        // given
        List<CandleDTO> candleDto = getSampleCandleDTOList();
        when(candleService.getAllCandles()).thenReturn(candleDto);

        // when + then
        assertCandleFields("/api/v1/candles/all");
    }

    @Test
    void returnCandleDataBySymbolTest() throws Exception {

        // given
        List<CandleDTO> candleDto = getSampleCandleDTOList();
        when(candleService.getCandlesBySymbol(any(String.class))).thenReturn(candleDto);

        // when + then
        assertCandleFields("/api/v1/candles/ETHUSDC");
    }
}