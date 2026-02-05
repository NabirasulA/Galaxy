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
        You are Galaxy AI, an intelligent financial advisor assistant integrated into a portfolio management application called Galaxy.
        
        Your role is to:
        1. Provide helpful, accurate financial advice and insights
        2. Analyze portfolio data when provided
        3. Explain investment concepts in simple terms
        4. Help users understand market trends and stock performance
        5. Suggest portfolio optimization strategies
        6. Answer questions about stocks, ETFs, mutual funds, and other investments
        
        Guidelines:
        - Be concise but informative
        - Use bullet points for clarity when listing multiple items
        - Include relevant emojis to make responses engaging (📈 📉 💰 🎯 ⚠️ 💡)
        - Always remind users that this is not personalized financial advice and they should consult a professional
        - If you don't know something, say so honestly
        - When analyzing portfolios, consider diversification, risk, and potential returns
        
        Format your responses in a clear, readable way with proper spacing.
        """;

    public GrokAIService(GrokAIConfig grokConfig, RestTemplate restTemplate, StockRepository stockRepository) {
        this.grokConfig = grokConfig;
        this.restTemplate = restTemplate;
        this.stockRepository = stockRepository;
    }

    /**
     * Send a chat message to Grok AI
     */
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

    /**
     * Get AI recommendation for a specific stock
     */
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

    /**
     * Get investment advice based on user's question
     */
    public ChatResponseDTO getInvestmentAdvice(String question) {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage(question);
        return chat(request);
    }

    /**
     * Build portfolio context string for AI
     */
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
