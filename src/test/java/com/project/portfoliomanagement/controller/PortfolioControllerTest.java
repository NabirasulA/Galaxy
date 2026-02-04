package com.project.portfoliomanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.portfoliomanagement.entity.Stock;
import com.project.portfoliomanagement.service.PortfolioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPortfolio_shouldReturnListOfStocks() throws Exception {
        Stock stock = new Stock(
                "AAPL",
                "Apple Inc",
                10,
                new BigDecimal("150.00")
        );

        when(portfolioService.getPortfolio())
                .thenReturn(List.of(stock));

        mockMvc.perform(get("/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].quantity").value(10));
    }

    @Test
    void addStock_shouldReturnCreatedStock() throws Exception {
        Stock stock = new Stock(
                "TSLA",
                "Tesla",
                5,
                new BigDecimal("300.00")
        );

        when(portfolioService.addorUpdateStock(any(Stock.class)))
                .thenReturn(stock);

        mockMvc.perform(post("/portfolio/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("TSLA"));
    }
}