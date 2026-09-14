package com.covira.backend.ai;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class AIQuestionService {

    private final Random random = new Random();

    /*
     * ============================================================
     * TEMPORARY STUB
     * ============================================================
     *
     * This is a temporary hardcoded version for demo purposes.
     * The real OpenAI-powered version requires an OPENAI_API_KEY
     * environment variable to be set.
     *
     * TODO: Replace with real OpenAI integration once API key
     * is available.
     */

    private static final List<String> SAMPLE_QUESTIONS = List.of(
            "Tell us about a challenging project you worked on and how you handled it.",
            "What interests you most about this role and our company?",
            "Describe a time you had to work under pressure to meet a deadline.",
            "How do you approach solving a problem you've never encountered before?",
            "What would your previous colleagues say is your greatest strength?",
            "Describe a situation where you had to collaborate with a difficult team member.",
            "Where do you see yourself professionally in the next few years?",
            "Walk us through how you prioritize tasks when everything feels urgent."
    );

    public String generateQuestion(
            String jobTitle,
            String category,
            String difficulty
    ) {

        /*
         * For the demo, we ignore jobTitle/category/difficulty
         * and just return a random sample question.
         */

        int index = random.nextInt(SAMPLE_QUESTIONS.size());

        return SAMPLE_QUESTIONS.get(index);
    }
}