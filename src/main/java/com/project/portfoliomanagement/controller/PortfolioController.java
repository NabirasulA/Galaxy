package com.project.portfoliomanagement.controller;

import com.project.portfoliomanagement.entity.Stock;
import com.project.portfoliomanagement.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    // --------------------------------
    // View portfolio (all stocks)
    // --------------------------------
    @GetMapping
    public ResponseEntity<List<Stock>> getPortfolio() {
        return ResponseEntity.ok(portfolioService.getPortfolio());
    }

    // --------------------------------
    // Add stock to portfolio
    // --------------------------------
    @PostMapping("/stock")
    public ResponseEntity<Stock> addStock(@RequestBody Stock stock) {
        Stock savedStock = portfolioService.addStock(stock);
        return new ResponseEntity<>(savedStock, HttpStatus.CREATED);
    }

    // --------------------------------
    // Update stock quantity
    // --------------------------------
    @PutMapping("/stock/{stockId}")
    public ResponseEntity<Stock> updateStockQuantity(
            @PathVariable Long stockId,
            @RequestParam Integer quantity) {

        Stock updatedStock = portfolioService.updateStockQuantity(stockId, quantity);
        return ResponseEntity.ok(updatedStock);
    }

    // --------------------------------
    // Remove stock from portfolio
    // --------------------------------
    @DeleteMapping("/stock/{stockId}")
    public ResponseEntity<Void> removeStock(@PathVariable Long stockId) {
        portfolioService.removeStock(stockId);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------
    // Sell stock (reduce quantity or delete if quantity becomes 0)
    // --------------------------------
    @PutMapping("/stock/{stockId}/sell")
    public ResponseEntity<Void> sellStock(
            @PathVariable Long stockId,
            @RequestParam Integer quantity) {

        portfolioService.sellStock(stockId, quantity);
        return ResponseEntity.noContent().build();
    }

    //get stock by symbol
     //--------------------------------
    @GetMapping("/stock/search")
    public ResponseEntity<Stock> getStockBySymbol(@RequestParam String symbol) {
        if((symbol==null)||symbol.isBlank()||symbol.isEmpty()){
            throw new RuntimeException("Symbol Must be provided");
        }
        Stock stock = portfolioService.getStockBySymbol (symbol);
        return ResponseEntity.ok(portfolioService.getStockBySymbol(symbol));
    }

    //Get todays gainers in stock market
    @GetMapping("/stock/gainers")
    public Object getTodaysGainers() {
        return portfolioService.getMarketData().get("top_gainers");
    }
    //get todays losers in stock market
    @GetMapping("/stock/losers")
    public Object getTodaysLosers() {
        return portfolioService.getMarketData().get("top_losers");
    }
    //get active stocks in stock market
    @GetMapping("/stock/active")
    public Object getActiveStocks() {
        return portfolioService.getMarketData().get("most_active");
    }
}
