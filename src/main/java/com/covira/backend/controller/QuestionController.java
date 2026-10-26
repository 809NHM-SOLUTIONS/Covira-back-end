package com.covira.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.covira.backend.entity.Question;
import com.covira.backend.service.InterviewService;
import com.covira.backend.service.QuestionService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class QuestionController {

    private final QuestionService questionService;
    private final InterviewService interviewService;

    public QuestionController(
            QuestionService questionService,
            InterviewService interviewService
    ) {
        this.questionService = questionService;
        this.interviewService = interviewService;
    }

    /*
     * Returns null when the logged-in employer owns the interview.
     * Otherwise returns the error response to send back.
     */
    private ResponseEntity<?> checkOwnership(
            HttpSession session,
            Long interviewId
    ) {

        Long employerId =
                (Long) session.getAttribute("loggedInUserId");

        if (employerId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        try {
            // Throws IllegalArgumentException when the interview
            // doesn't exist or belongs to another employer.
            interviewService.getInterview(employerId, interviewId);
            return null;

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Interview not found.");
        }
    }

    /*
     * PUBLIC (candidates, no login)
     * GET: /api/interviews/public/{token}/questions
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

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/interviews/{interviewId}/questions")
    public ResponseEntity<?> createQuestion(
            @PathVariable Long interviewId,
            @RequestBody QuestionRequest request,
            HttpSession session
    ) {

        ResponseEntity<?> denied = checkOwnership(session, interviewId);
        if (denied != null) return denied;

        try {

            Question question =
                    questionService.createQuestion(
                            interviewId,
                            request.questionText(),
                            request.questionType(),
                            request.timeLimit(),
                            request.options(),
                            request.allowMultiple(),
                              request.required()
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
     * PUT: /api/interviews/{interviewId}/questions/reorder
     * Body: { "questionIds": [3, 1, 2] }  (new order, first to last)
     */
    @PutMapping("/interviews/{interviewId}/questions/reorder")
    public ResponseEntity<?> reorderQuestions(
            @PathVariable Long interviewId,
            @RequestBody ReorderRequest request,
            HttpSession session
    ) {

        ResponseEntity<?> denied = checkOwnership(session, interviewId);
        if (denied != null) return denied;

        try {

            return ResponseEntity.ok(
                    questionService.reorderQuestions(
                            interviewId,
                            request.questionIds()
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/interviews/{interviewId}/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long interviewId,
            @PathVariable Long questionId,
            @RequestBody QuestionRequest request,
            HttpSession session
    ) {

        ResponseEntity<?> denied = checkOwnership(session, interviewId);
        if (denied != null) return denied;

        try {

            Question question =
                    questionService.updateQuestion(
                            interviewId,
                            questionId,
                            request.questionText(),
                            request.questionType(),
                            request.timeLimit(),
                            request.options(),
                            request.allowMultiple(),
                                request.required()
                    );

            return ResponseEntity.ok(question);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/interviews/{interviewId}/questions")
    public ResponseEntity<?> getQuestions(
            @PathVariable Long interviewId,
            HttpSession session
    ) {

        ResponseEntity<?> denied = checkOwnership(session, interviewId);
        if (denied != null) return denied;

        return ResponseEntity.ok(
                questionService.getQuestions(interviewId)
        );
    }

    @GetMapping("/interviews/{interviewId}/questions/locked")
    public ResponseEntity<?> isLocked(
            @PathVariable Long interviewId,
            HttpSession session
    ) {

        ResponseEntity<?> denied = checkOwnership(session, interviewId);
        if (denied != null) return denied;

        return ResponseEntity.ok(
                Map.of("locked", questionService.isLocked(interviewId))
        );
    }

    @GetMapping("/interviews/{interviewId}/questions/{questionId}")
    public ResponseEntity<?> getQuestion(
            @PathVariable Long interviewId,
            @PathVariable Long questionId,
            HttpSession session
    ) {

        ResponseEntity<?> denied = checkOwnership(session, interviewId);
        if (denied != null) return denied;

        try {

            return ResponseEntity.ok(
                    questionService.getQuestion(interviewId, questionId)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/interviews/{interviewId}/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable Long interviewId,
            @PathVariable Long questionId,
            HttpSession session
    ) {

        ResponseEntity<?> denied = checkOwnership(session, interviewId);
        if (denied != null) return denied;

        try {

            questionService.deleteQuestion(interviewId, questionId);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {

            // e.g. the interview is locked
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    public record ReorderRequest(List<Long> questionIds) {
    }

    public record QuestionRequest(
            String questionText,
            String questionType,
            Integer timeLimit,
            List<String> options,
            Boolean allowMultiple,
            Boolean required
    ) {
    }
}