package com.project.portfoliomanagement.service;

import com.project.portfoliomanagement.entity.Stock;
import com.project.portfoliomanagement.exception.ResourceNotFoundException;
import com.project.portfoliomanagement.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    @Value("${alphavantage.base.url}")
    private String baseUrl;

    private static final RestTemplate restTemplate = new RestTemplate();

    // cache for demo stability
    private Map<String, Object> cachedResponse;

    public Map<String, Object> getMarketData() {

        if (cachedResponse != null) {
            return cachedResponse;
        }

        String url = "https://www.alphavantage.co/query"+"?function=TOP_GAINERS_LOSERS"
                + "&apikey=" + apiKey;

        cachedResponse = restTemplate.getForObject(url, Map.class);
        return cachedResponse;
    }

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
    // Add stock OR update existing stock with avg price recalculation
    // -----------------------------
    public Stock addorUpdateStock(Stock newStock) {

        Stock existingStock = stockRepository.findBySymbol(newStock.getSymbol())
                .orElse(null);

        // If stock already exists → recalculate average buy price
        if (existingStock != null) {

            int oldQty = existingStock.getQuantity();
            int newQty = newStock.getQuantity();
            int totalQty = oldQty + newQty;

            BigDecimal oldPrice = existingStock.getBuyPrice(); // BigDecimal
            BigDecimal newPrice = newStock.getBuyPrice();       // BigDecimal

            // (oldQty × oldPrice) + (newQty × newPrice)
            BigDecimal totalCost =
                    oldPrice.multiply(BigDecimal.valueOf(oldQty))
                            .add(newPrice.multiply(BigDecimal.valueOf(newQty)));

            // avgPrice = totalCost / totalQty
            BigDecimal avgPrice =
                    totalCost.divide(
                            BigDecimal.valueOf(totalQty),
                            2,                // scale (2 decimal places)
                            RoundingMode.HALF_UP
                    );

            existingStock.setQuantity(totalQty);
            existingStock.setBuyPrice(avgPrice);

            return stockRepository.save(existingStock);
        }

        // First-time buy
        return stockRepository.save(newStock);
    }




    @Value("${ipoalerts.api.key}")
    private static String ipoApiKey;


    private static HttpHeaders createIPOHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", ipoApiKey);
        return headers;
    }

    public static Map<String, Object> getIPOs(String status, String type, int page, int limit) {

        String url = "https://api.ipoalerts.in/ipos?page=" + page + "&limit=" + limit;

        if (status != null)
            url += "&status=" + status;

        if (type != null)
            url += "&type=" + type;

        HttpEntity<String> entity = new HttpEntity<>(createIPOHeaders());

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }

    public static Map<String, Object> getIPODetails(String identifier) {

        String url = "https://api.ipoalerts.in/ipos/" + identifier;

        HttpEntity<String> entity = new HttpEntity<>(createIPOHeaders());

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return response.getBody();
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


    //Search stock by symbol
    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found with symbol: " + symbol
                ));
    }
}
