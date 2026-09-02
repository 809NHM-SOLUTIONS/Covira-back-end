package com.covira.backend.controller;

import com.covira.backend.entity.Question;
import com.covira.backend.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(
            QuestionService questionService
    ) {
        this.questionService = questionService;
    }

    /*
     * ============================================================
     * PUBLIC INTERVIEW QUESTIONS
     * ============================================================
     *
     * Candidates do not need to be logged in.
     *
     * GET:
     * /api/interviews/public/{token}/questions
     * ============================================================
     */

    @GetMapping("/interviews/public/{token}/questions")
    public ResponseEntity<?> getPublicQuestions(
            @PathVariable String token
    ) {

        try {

            List<Question> questions =
                    questionService.getPublicQuestions(token);

            return ResponseEntity.ok(questions);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /*
     * ============================================================
     * ADD QUESTION TO INTERVIEW
     * ============================================================
     *
     * POST:
     * /api/interviews/{interviewId}/questions
     * ============================================================
     */

    @PostMapping("/interviews/{interviewId}/questions")
    public ResponseEntity<?> createQuestion(
            @PathVariable Long interviewId,
            @RequestBody QuestionRequest request
    ) {

        try {

            Question question =
                    questionService.createQuestion(
                            interviewId,
                            request.questionText(),
                            request.questionType(),
                            request.timeLimit()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(question);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    /*
     * ============================================================
     * GET INTERVIEW QUESTIONS
     * ============================================================
     *
     * GET:
     * /api/interviews/{interviewId}/questions
     * ============================================================
     */

    @GetMapping("/interviews/{interviewId}/questions")
    public ResponseEntity<?> getQuestions(
            @PathVariable Long interviewId
    ) {

        try {

            List<Question> questions =
                    questionService.getQuestions(
                            interviewId
                    );

            return ResponseEntity.ok(questions);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /*
     * ============================================================
     * GET ONE QUESTION
     * ============================================================
     *
     * GET:
     * /api/interviews/{interviewId}/questions/{questionId}
     * ============================================================
     */

    @GetMapping(
            "/interviews/{interviewId}/questions/{questionId}"
    )
    public ResponseEntity<?> getQuestion(
            @PathVariable Long interviewId,
            @PathVariable Long questionId
    ) {

        try {

            Question question =
                    questionService.getQuestion(
                            interviewId,
                            questionId
                    );

            return ResponseEntity.ok(question);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /*
     * ============================================================
     * DELETE QUESTION
     * ============================================================
     *
     * DELETE:
     * /api/interviews/{interviewId}/questions/{questionId}
     * ============================================================
     */

    @DeleteMapping(
            "/interviews/{interviewId}/questions/{questionId}"
    )
    public ResponseEntity<?> deleteQuestion(
            @PathVariable Long interviewId,
            @PathVariable Long questionId
    ) {

        try {

            questionService.deleteQuestion(
                    interviewId,
                    questionId
            );

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /*
     * ============================================================
     * REQUEST DTO
     * ============================================================
     */

    public record QuestionRequest(
            String questionText,
            String questionType,
            Integer timeLimit
    ) {
    }
        @GetMapping("/interviews/{interviewId}/questions/locked")
    public ResponseEntity<?> isLocked(
            @PathVariable Long interviewId
    ) {

        try {

            boolean locked = questionService.isLocked(interviewId);

            return ResponseEntity.ok(java.util.Map.of("locked", locked));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}