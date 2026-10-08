package com.covira.backend.controller;

import com.covira.backend.dto.CandidateDto;
import com.covira.backend.service.CandidateService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(
            CandidateService candidateService
    ) {
        this.candidateService = candidateService;
    }

    /*
     * ============================================================
     * GET ALL CANDIDATES
     * ============================================================
     */

    @GetMapping
    public ResponseEntity<?> getCandidates(
            HttpSession session
    ) {

        Long employerId =
                getEmployerId(session);

        if (employerId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        List<CandidateDto> candidates =
                candidateService
                        .getCandidates(employerId);

        return ResponseEntity.ok(candidates);
    }


    /*
     * ============================================================
     * GET ONE CANDIDATE
     * ============================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<?> getCandidate(
            @PathVariable Long id,
            HttpSession session
    ) {

        Long employerId =
                getEmployerId(session);

        if (employerId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        try {

            return ResponseEntity.ok(
                    candidateService.getCandidate(
                            employerId,
                            id
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }


    /*
     * ============================================================
     * PUBLIC CANDIDATE CREATION
     * ============================================================
     */

    @PostMapping("/public")
    public ResponseEntity<?> createPublicCandidate(
            @RequestBody Map<String, String> request
    ) {

        try {

            String interviewToken =
                    request.get("interviewToken");

            String name =
                    request.get("name");

            String email =
                    request.get("email");

            if (interviewToken == null ||
                    interviewToken.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Interview token is required."
                        );
            }

            CandidateDto candidate =
                    candidateService
                            .createPublicCandidate(
                                    interviewToken,
                                    name,
                                    email
                            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(candidate);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            // The candidate deadline has passed.
            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body(e.getMessage());
        }
    }


    /*
     * ============================================================
     * SUBMIT INTERVIEW
     * ============================================================
     */

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitInterview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> answers =
                    (Map<String, Object>)
                            request.get("answers");

            CandidateDto candidate =
                    candidateService.submitInterview(
                        
                            id,
                            answers
                    );

            return ResponseEntity.ok(candidate);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "Failed to submit interview."
                    );
        }
    }


    /*
     * ============================================================
     * UPLOAD VIDEO RESPONSE
     * ============================================================
     *
     * POST:
     *
     * /api/candidates/{candidateId}/responses/video
     *
     * Form data:
     *
     * interviewToken
     * questionNumber
     * video
     *
     * ============================================================
     */

    @PostMapping(
            value = "/{candidateId}/responses/video",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadVideoResponse(
            @PathVariable Long candidateId,

            @RequestParam("interviewToken")
            String interviewToken,

            @RequestParam("questionNumber")
            Integer questionNumber,

            @RequestParam("video")
            MultipartFile video
    ) {

        try {

            CandidateDto candidate =
                    candidateService
                            .uploadVideoResponse(
                                    candidateId,
                                    interviewToken,
                                    questionNumber,
                                    video
                            );

            return ResponseEntity.ok(candidate);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "Failed to upload video response."
                    );
        }
    }


    /*
     * ============================================================
     * RECORD REVIEW DECISION (SHORTLIST / REJECT)
     * ============================================================
     *
     * POST /api/candidates/{id}/decision
     * Body: { "decision": "SHORTLIST" | "REJECT" }
     * ============================================================
     */

    @PostMapping("/{id}/decision")
    public ResponseEntity<?> recordDecision(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session
    ) {

        Long employerId =
                getEmployerId(session);

        if (employerId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        try {

            CandidateDto candidate =
                    candidateService.recordDecision(
                            employerId,
                            id,
                            request.get("decision")
                    );

            return ResponseEntity.ok(candidate);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "Failed to record decision."
                    );
        }
    }


    /*
     * ============================================================
     * SESSION USER
     * ============================================================
     */

    private Long getEmployerId(
            HttpSession session
    ) {

        return (Long) session.getAttribute(
                "loggedInUserId"
        );
    }
}