package com.project.portfoliomanagement.repository;

import com.project.portfoliomanagement.entity.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Test
    void findBySymbol_shouldReturnStock_whenExists() {
        Stock stock = new Stock(
                "AAPL",
                "Apple Inc",
                10,
                new BigDecimal("150.00")
        );

        stockRepository.save(stock);

        Optional<Stock> result = stockRepository.findBySymbol("AAPL");

        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("AAPL");
        assertThat(result.get().getQuantity()).isEqualTo(10);
    }

    @Test
    void findBySymbol_shouldReturnEmpty_whenNotExists() {
        Optional<Stock> result = stockRepository.findBySymbol("TSLA");
        assertThat(result).isEmpty();
    }
}