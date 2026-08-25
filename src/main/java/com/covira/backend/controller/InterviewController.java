package com.covira.backend.controller;

import com.covira.backend.dto.InterviewCreateRequest;
import com.covira.backend.entity.Interview;
import com.covira.backend.service.InterviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "http://localhost:5173")
public class InterviewController {

    private final InterviewService interviewService;


    public InterviewController(
            InterviewService interviewService) {

        this.interviewService = interviewService;
    }


    // =========================================================
    // CREATE INTERVIEW
    // =========================================================

    @PostMapping
    public Interview createInterview(
            @RequestBody InterviewCreateRequest request) {

        return interviewService.createInterview(
                request
        );
    }


    // =========================================================
    // GET EMPLOYER'S INTERVIEWS
    // =========================================================

    @GetMapping
    public List<Interview> getEmployerInterviews(
            @RequestParam String employerEmail) {

        return interviewService
                .getEmployerInterviews(
                        employerEmail
                );
    }


    // =========================================================
    // GET INTERVIEW BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Interview getInterview(
            @PathVariable Long id) {

        return interviewService
                .getInterviewById(id);
    }


    // =========================================================
    // GET INTERVIEW FOR CANDIDATE
    // =========================================================

    @GetMapping("/candidate/{token}")
    public Interview getCandidateInterview(
            @PathVariable String token) {

        return interviewService
                .getInterviewByToken(token);
    }


    // =========================================================
    // UPDATE INTERVIEW
    // =========================================================

    @PutMapping("/{id}")
    public Interview updateInterview(
            @PathVariable Long id,
            @RequestBody InterviewCreateRequest request) {

        return interviewService
                .updateInterview(
                        id,
                        request
                );
    }


    // =========================================================
    // PUBLISH
    // =========================================================

    @PutMapping("/{id}/publish")
    public Interview publishInterview(
            @PathVariable Long id) {

        return interviewService
                .publishInterview(id);
    }


    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteInterview(
            @PathVariable Long id) {

        interviewService
                .deleteInterview(id);
    }
}