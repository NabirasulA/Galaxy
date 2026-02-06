package com.project.portfoliomanagement.service;

import com.project.portfoliomanagement.config.GrokAIConfig;
import com.project.portfoliomanagement.dto.*;
import com.project.portfoliomanagement.entity.Stock;
import com.project.portfoliomanagement.repository.StockRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GrokAIService {

    private final GrokAIConfig grokConfig;
    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;

    private static final String SYSTEM_PROMPT = """
        You are Galaxy AI,Context:
        You are an intelligent financial advisor embedded in a portfolio management application. Your role is to provide in-depth financial analysis, help with optimizing investment strategies, and ensure the user has a clear understanding of their portfolio performance. Your primary objective is to generate a detailed portfolio analysis that highlights top-performing assets, underperforming assets, diversification status, risk levels, and potential improvements. Always remind the user that your analysis is not personalized financial advice and they should consult a professional for tailored recommendations.
        User Input:
                              Provide me with a detailed analysis of my investment portfolio. I want to understand:
            
                              Which stocks have contributed the most to the profits.
            
                              Which stocks have contributed to losses.
            
                              The overall diversification of my portfolio (across sectors, asset classes, etc.).
            
                              A risk analysis of my portfolio.
            
                              Suggestions for potential improvements or optimizations.
            
                              AI Bot Response:
            
                              Hello! Let's dive into your portfolio analysis. 📊 I’ll break it down step-by-step to give you a clear picture.
            
                              1. Top-Performing Stocks 📈:
                              These are the stocks that have delivered the highest returns. I’ll calculate their contribution to profits, considering how much each has appreciated in value since your purchase.
            
                              Stock A (Ticker): +25% return. This stock has been the major contributor to your portfolio's growth. It's been performing exceptionally well, especially in the past quarter. 📈
            
                              Stock B (Ticker): +18% return. A solid performer in your portfolio, contributing significantly to your overall profit. 🎯
            
                              2. Underperforming Stocks 📉:
                              Now, let's look at the stocks that have been dragging down the portfolio’s performance. These could be the stocks that are currently at a loss or not delivering the expected returns.
            
                              Stock C (Ticker): -12% return. This stock has been contributing the most to your losses. It might be worth investigating the market trends or company performance.
            
                              Stock D (Ticker): -8% return. While not as deep in the red, this stock is still underperforming and affecting your overall returns.
            
                              3. Portfolio Diversification 💡:
                              Let’s check how diversified your portfolio is:
            
                              Sectors: Are you overly concentrated in one sector? For example, your portfolio seems heavily weighted in Technology (50%) and Healthcare (30%). Diversifying into other sectors like Energy (10%) and Financials (10%) could reduce your risk.
            
                              Asset Classes: It looks like your portfolio is 90% equity and 10% bonds/cash. If you’re risk-averse, you may want to consider more bonds or dividend-paying stocks to reduce volatility.
            
                              4. Risk Assessment ⚠️:
                              Let’s evaluate the overall risk of your portfolio. Here’s what stands out:
            
                              Volatility: Your portfolio shows a high level of volatility, particularly because of your concentrated positions in growth stocks like Stock A and Stock B. These stocks tend to fluctuate more than traditional dividend stocks.
            
                              Concentration Risk: You’re significantly exposed to a single sector, Technology. A downturn in this sector could lead to substantial losses. Consider diversifying into other industries.
            
                              Time Horizon: Based on your holdings, it seems like your investment horizon is long-term, but it’s important to ensure you’re comfortable with the risk level.
            
                              5. Suggestions for Optimization 💰:
                              Based on the analysis, here are some potential steps you can take to improve your portfolio:
            
                              Diversify Sector Allocation: Consider adding stocks or ETFs from sectors like Energy, Consumer Staples, or Utilities to balance out your portfolio and reduce sector risk.
            
                              Rebalance Positions: If Stock C and Stock D continue to underperform, you might want to either sell them off or reinvest in stronger-performing assets.
            
                              Introduce More Fixed-Income Assets: If you're looking to reduce risk, allocating more funds into bonds or dividend-paying stocks might make your portfolio less volatile.
            
                              Consider International Exposure: If your portfolio is heavily U.S.-focused, adding some international stocks or global ETFs could bring in a layer of diversification.
            
                              Final Note: Remember, this analysis is based on the data you’ve provided, and while it highlights general trends, it’s essential to consult with a professional financial advisor to tailor a strategy based on your unique goals and risk tolerance. 📈💡
            
                              Feel free to update me with any further details or adjustments you'd like to make to the analysis! I’m here to help optimize your portfolio to meet your financial goals.
            
                              User Follow-up (if needed): If you want to adjust specific metrics or focus more on certain aspects (e.g., sector performance, specific stocks), let me know, and I can refine the analysis further!
            
                              Additional Prompt Ideas:
            
                              Portfolio Rebalancing Request:
            
                              “Can you suggest a rebalancing strategy for my portfolio, focusing on minimizing risk while maximizing returns?”
            
                              Long-term Growth Focus:
            
                              “I’m looking to focus on long-term growth. How can I optimize my portfolio for maximum returns over the next 5-10 years?”
            
                              Risk Reduction Strategy:
            
                              “I’m feeling a bit uncomfortable with the level of risk in my portfolio. Can you suggest ways to reduce my risk exposure?”
            
                              Let me know if you want to further refine or modify the prompt for other types of analysis! 📊
        
        Format your responses in a clear, readable way with proper spacing.
        """;

    public GrokAIService(GrokAIConfig grokConfig, RestTemplate restTemplate, StockRepository stockRepository) {
        this.grokConfig = grokConfig;
        this.restTemplate = restTemplate;
        this.stockRepository = stockRepository;
    }


    public ChatResponseDTO chat(ChatRequestDTO request) {
        try {
            String apiKey = grokConfig.getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return ChatResponseDTO.error("Grok API key is not configured. Please add 'grok.api.key' to application.properties");
            }

            // Build messages list
            List<GrokRequestDTO.Message> messages = new ArrayList<>();
            messages.add(new GrokRequestDTO.Message("system", SYSTEM_PROMPT));

            // Add portfolio context if available
            String portfolioContext = buildPortfolioContext();
            if (portfolioContext != null && !portfolioContext.isEmpty()) {
                messages.add(new GrokRequestDTO.Message("system",
                    "Current user portfolio context:\n" + portfolioContext));
            }

            // Add user's custom context if provided
            if (request.getContext() != null && !request.getContext().isEmpty()) {
                messages.add(new GrokRequestDTO.Message("system",
                    "Additional context: " + request.getContext()));
            }

            // Add user message
            messages.add(new GrokRequestDTO.Message("user", request.getMessage()));

            // Build request
            GrokRequestDTO grokRequest = new GrokRequestDTO();
            grokRequest.setMessages(messages);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<GrokRequestDTO> entity = new HttpEntity<>(grokRequest, headers);

            // Make API call
            ResponseEntity<GrokResponseDTO> response = restTemplate.exchange(
                grokConfig.getApiUrl(),
                HttpMethod.POST,
                entity,
                GrokResponseDTO.class
            );

            if (response.getBody() != null &&
                response.getBody().getChoices() != null &&
                !response.getBody().getChoices().isEmpty()) {

                String aiResponse = response.getBody().getChoices().get(0).getMessage().getContent();
                return ChatResponseDTO.success(aiResponse);
            }

            return ChatResponseDTO.error("No response received from AI");

        } catch (Exception e) {
            return ChatResponseDTO.error("Failed to get AI response: " + e.getMessage());
        }
    }

    /**
     * Get AI analysis of the current portfolio
     */
    public ChatResponseDTO analyzePortfolio() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("Please analyze my current portfolio and provide insights on:\n" +
            "1. Overall portfolio health\n" +
            "2. Diversification analysis\n" +
            "3. Risk assessment\n" +
            "4. Specific recommendations for improvement\n" +
            "5. Any concerns or warnings");

        return chat(request);
    }

    public ChatResponseDTO getStockAnalysis(String symbol) {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("Please provide a brief analysis of " + symbol + " stock including:\n" +
            "1. Company overview\n" +
            "2. Recent performance trends\n" +
            "3. Key factors to consider\n" +
            "4. Is it a good buy/hold/sell right now?\n" +
            "Note: Base your analysis on general market knowledge.");

        return chat(request);
    }


    public ChatResponseDTO getInvestmentAdvice(String question) {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage(question);
        return chat(request);
    }


    private String buildPortfolioContext() {
        try {
            List<Stock> stocks = stockRepository.findAll();
            if (stocks.isEmpty()) {
                return "User has no stocks in their portfolio yet.";
            }

            StringBuilder context = new StringBuilder();
            context.append("Portfolio Holdings:\n");

            BigDecimal totalInvested = BigDecimal.ZERO;

            for (Stock stock : stocks) {
                BigDecimal invested = stock.getTotalInvestedAmount();
                if (invested == null) {
                    invested = stock.getBuyPrice().multiply(BigDecimal.valueOf(stock.getQuantity()));
                }
                totalInvested = totalInvested.add(invested);

                context.append(String.format("- %s (%s): %d shares @ $%.2f avg cost (Total: $%.2f)\n",
                    stock.getSymbol(),
                    stock.getCompanyName() != null ? stock.getCompanyName() : "N/A",
                    stock.getQuantity(),
                    stock.getBuyPrice(),
                    invested));
            }

            context.append(String.format("\nTotal Portfolio Investment: $%.2f\n", totalInvested));
            context.append(String.format("Number of Holdings: %d\n", stocks.size()));

            // Calculate sector diversity (simplified)
            List<String> symbols = stocks.stream()
                .map(Stock::getSymbol)
                .collect(Collectors.toList());
            context.append("Symbols held: ").append(String.join(", ", symbols));

            return context.toString();

        } catch (Exception e) {
            return "Unable to fetch portfolio data.";
        }
    }
}
