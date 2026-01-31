package com.project.portfoliomanagement.service;

import com.project.portfoliomanagement.entity.Stock;
import com.project.portfoliomanagement.exception.ResourceNotFoundException;
import com.project.portfoliomanagement.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private final StockRepository stockRepository;

    public PortfolioService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // -----------------------------
    // Get all stocks in portfolio
    // -----------------------------
    public List<Stock> getPortfolio() {
        return stockRepository.findAll();
    }

    // -----------------------------
    // Add a new stock
    // -----------------------------
    public Stock addStock(Stock stock) {
        return stockRepository.save(stock);
    }

    // -----------------------------
    // Update stock quantity
    // -----------------------------
    public Stock updateStockQuantity(Long stockId, Integer quantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Stock not found with id: " + stockId
        ));

        stock.setQuantity(quantity);
        return stockRepository.save(stock);
    }

    // -----------------------------
    // Remove stock from portfolio
    // -----------------------------
    public void removeStock(Long stockId) {
        if (!stockRepository.existsById(stockId)) {
            throw new ResourceNotFoundException(
                    "Stock not found with id: " + stockId
            );
        }
        stockRepository.deleteById(stockId);
    }
}