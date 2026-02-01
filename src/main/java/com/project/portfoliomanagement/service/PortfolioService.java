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
    // Sell stock (reduce quantity or delete if quantity becomes 0)
    // -----------------------------
    public void sellStock(Long stockId, Integer sellQuantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found with id: " + stockId
                ));

        if (sellQuantity <= 0) {
            throw new IllegalArgumentException("Sell quantity must be positive");
        }

        if (stock.getQuantity() < sellQuantity) {
            throw new IllegalArgumentException("Not enough stock to sell");
        }

        // Decrease quantity
        stock.setQuantity(stock.getQuantity() - sellQuantity);

        // If quantity becomes 0, delete the stock
        if (stock.getQuantity() == 0) {
            stockRepository.delete(stock);
        } else {
            stockRepository.save(stock);
        }
    }

    // -----------------------------
    // Remove stock from portfolio (only if quantity is 0)
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
