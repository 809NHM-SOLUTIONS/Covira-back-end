package com.covira.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.covira.backend.ai.AIQuestionService;
import com.covira.backend.dto.AIQuestionRequest;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AIQuestionController {

    private final AIQuestionService aiQuestionService;

    public AIQuestionController(AIQuestionService aiQuestionService) {
        this.aiQuestionService = aiQuestionService;
    }

    @PostMapping("/generate-question")
    public ResponseEntity<?> generateQuestion(
            @RequestBody AIQuestionRequest request) {

        try {

            String question = aiQuestionService.generateQuestion(
                    request.getJobTitle(),
                    request.getCategory(),
                    request.getDifficulty()
            );

            return ResponseEntity.ok(
                    Map.of("question", question)
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "error", "Failed to generate question",
                            "message", e.getMessage()
                    )
            );
        }
    }
}