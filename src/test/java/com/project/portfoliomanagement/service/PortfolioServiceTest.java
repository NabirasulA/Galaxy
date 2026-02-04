package com.project.portfoliomanagement.service;

import com.project.portfoliomanagement.entity.Stock;
import com.project.portfoliomanagement.repository.PortfolioSnapshotRepository;
import com.project.portfoliomanagement.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void addOrUpdateStock_shouldUpdateQuantityAndAvgPrice() {
        Stock existing = new Stock(
                "AAPL",
                "Apple Inc",
                10,
                new BigDecimal("100.00")
        );

        when(stockRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(existing));

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Stock newStock = new Stock(
                "AAPL",
                "Apple Inc",
                5,
                new BigDecimal("200.00")
        );

        Stock result = portfolioService.addorUpdateStock(newStock);

        assertThat(result.getQuantity()).isEqualTo(15);
        assertThat(result.getBuyPrice())
                .isEqualByComparingTo("133.33");
    }

    @Test
    void addOrUpdateStock_shouldCreateNewStock_whenNotExists() {
        when(stockRepository.findBySymbol("TSLA"))
                .thenReturn(Optional.empty());

        Stock stock = new Stock(
                "TSLA",
                "Tesla",
                5,
                new BigDecimal("300.00")
        );

        when(stockRepository.save(stock)).thenReturn(stock);

        Stock result = portfolioService.addorUpdateStock(stock);

        assertThat(result.getSymbol()).isEqualTo("TSLA");
        verify(stockRepository).save(stock);
    }
}