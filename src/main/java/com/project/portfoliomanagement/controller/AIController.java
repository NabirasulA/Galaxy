package com.project.portfoliomanagement.controller;

import com.project.portfoliomanagement.dto.ChatRequestDTO;
import com.project.portfoliomanagement.dto.ChatResponseDTO;
import com.project.portfoliomanagement.service.GrokAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final GrokAIService grokAIService;

    public AIController(GrokAIService grokAIService) {
        this.grokAIService = grokAIService;
    }


    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO response = grokAIService.chat(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/analyze-portfolio")
    public ResponseEntity<ChatResponseDTO> analyzePortfolio() {
        ChatResponseDTO response = grokAIService.analyzePortfolio();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/analyze-stock")
    public ResponseEntity<ChatResponseDTO> analyzeStock(@RequestParam String symbol) {
        ChatResponseDTO response = grokAIService.getStockAnalysis(symbol.toUpperCase());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/advice")
    public ResponseEntity<ChatResponseDTO> getAdvice(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO response = grokAIService.getInvestmentAdvice(request.getMessage());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/health")
    public ResponseEntity<ChatResponseDTO> healthCheck() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("Say 'Galaxy AI is online and ready to help! 🚀' in exactly those words.");
        ChatResponseDTO response = grokAIService.chat(request);
        return ResponseEntity.ok(response);
    }
}
