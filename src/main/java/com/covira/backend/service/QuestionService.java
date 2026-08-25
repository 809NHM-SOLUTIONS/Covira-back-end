package com.covira.backend.service;

import com.covira.backend.entity.Question;
import com.covira.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }


    // =========================================================
    // CREATE QUESTION
    // =========================================================

    public Question createQuestion(Question question) {

        if (question.getCreationMethod() == null ||
                question.getCreationMethod().isBlank()) {

            question.setCreationMethod("MANUAL");
        }

        return questionRepository.save(question);
    }


    // =========================================================
    // GET QUESTIONS FOR EMPLOYER
    // =========================================================

    public List<Question> getQuestionsByUserEmail(
            String userEmail) {

        if (userEmail == null ||
                userEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "Employer email is required."
            );
        }

        return questionRepository
                .findByUserEmail(userEmail);
    }


    // =========================================================
    // GET ONE QUESTION FOR EMPLOYER
    // =========================================================

    public Question getQuestionById(
            Long id,
            String userEmail) {

        return questionRepository
                .findByIdAndUserEmail(
                        id,
                        userEmail
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Question not found."
                        ));
    }


    // =========================================================
    // UPDATE QUESTION
    // =========================================================

    public Question updateQuestion(
            Long id,
            String userEmail,
            Question updatedQuestion) {

        Question question =
                getQuestionById(
                        id,
                        userEmail
                );


        question.setQuestionText(
                updatedQuestion.getQuestionText()
        );

        question.setCategory(
                updatedQuestion.getCategory()
        );

        question.setDifficulty(
                updatedQuestion.getDifficulty()
        );

        question.setResponseDuration(
                updatedQuestion.getResponseDuration()
        );

        question.setCreationMethod(
                updatedQuestion.getCreationMethod()
        );


        return questionRepository.save(
                question
        );
    }


    // =========================================================
    // DELETE QUESTION
    // =========================================================

    public void deleteQuestion(
            Long id,
            String userEmail) {

        Question question =
                getQuestionById(
                        id,
                        userEmail
                );

        questionRepository.delete(
                question
        );
    }
}