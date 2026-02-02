package com.project.portfoliomanagement.controller;

import com.project.portfoliomanagement.entity.Stock;
import com.project.portfoliomanagement.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    // Add or Update stock to portfolio
    // --------------------------------
    @PostMapping("/stock")
    public ResponseEntity<Stock> addStock(@Valid @RequestBody Stock stock) {
        Stock saved = portfolioService.addorUpdateStock(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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



    @GetMapping("/stock/ipos")
    public Map<String, Object> getIPOs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int limit) {

        return PortfolioService.getIPOs(status, type, page, limit);
    }

    @GetMapping("/stock/ipos/{identifier}")
    public Map<String, Object> getIPO(@PathVariable String identifier) {
        return PortfolioService.getIPODetails(identifier);
    }
}
