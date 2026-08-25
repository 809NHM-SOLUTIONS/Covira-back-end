package com.covira.backend.controller;

import com.covira.backend.entity.Question;
import com.covira.backend.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:5173")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(
            QuestionService questionService) {

        this.questionService = questionService;
    }


    // =========================================================
    // CREATE QUESTION
    // =========================================================

    @PostMapping
    public Question createQuestion(
            @RequestBody Question question) {

        return questionService.createQuestion(
                question
        );
    }


    // =========================================================
    // GET EMPLOYER'S QUESTIONS
    // =========================================================

    @GetMapping
    public List<Question> getQuestionsByUserEmail(
            @RequestParam String email) {

        return questionService
                .getQuestionsByUserEmail(email);
    }


    // =========================================================
    // GET ONE QUESTION
    // =========================================================

    @GetMapping("/{id}")
    public Question getQuestion(
            @PathVariable Long id,
            @RequestParam String email) {

        return questionService
                .getQuestionById(
                        id,
                        email
                );
    }


    // =========================================================
    // UPDATE QUESTION
    // =========================================================

    @PutMapping("/{id}")
    public Question updateQuestion(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestBody Question question) {

        return questionService
                .updateQuestion(
                        id,
                        email,
                        question
                );
    }


    // =========================================================
    // DELETE QUESTION
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteQuestion(
            @PathVariable Long id,
            @RequestParam String email) {

        questionService
                .deleteQuestion(
                        id,
                        email
                );
    }
}