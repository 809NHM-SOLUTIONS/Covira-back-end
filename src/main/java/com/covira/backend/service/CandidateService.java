package com.covira.backend.service;


import com.covira.backend.dto.CandidateDto;
import com.covira.backend.dto.CandidateResponseDto;
import com.covira.backend.entity.Candidate;
import com.covira.backend.entity.CandidateResponse;
import com.covira.backend.entity.Interview;
import com.covira.backend.entity.Question;
import com.covira.backend.repository.CandidateRepository;
import com.covira.backend.repository.CandidateResponseRepository;
import com.covira.backend.repository.InterviewRepository;
import com.covira.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.covira.backend.notification.NotificationType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CandidateService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final CandidateResponseRepository candidateResponseRepository;
    private final QuestionRepository questionRepository;
        private final NotificationService notificationService;

    public CandidateService(
            CandidateRepository candidateRepository,
            InterviewRepository interviewRepository,
            CandidateResponseRepository candidateResponseRepository,
            QuestionRepository questionRepository,
            NotificationService notificationService
    ) {
        this.candidateRepository = candidateRepository;
        this.interviewRepository = interviewRepository;
        this.candidateResponseRepository = candidateResponseRepository;
        this.questionRepository = questionRepository;
        this.notificationService = notificationService;
}


    /*
     * ============================================================
     * UPLOAD VIDEO RESPONSE
     * ============================================================
     */

    @Transactional
    public CandidateDto uploadVideoResponse(
            Long candidateId,
            String interviewToken,
            Integer questionNumber,
            MultipartFile video
    ) {

        if (video == null || video.isEmpty()) {
            throw new IllegalArgumentException(
                    "Video file is required."
            );
        }

        if (questionNumber == null) {
            throw new IllegalArgumentException(
                    "Question number is required."
            );
        }

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found."
                                )
                        );

        Interview interview =
                interviewRepository
                        .findByInterviewToken(interviewToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interview link is invalid or expired."
                                )
                        );


        /*
         * Make sure the candidate belongs to the
         * employer that owns this interview.
         */

        if (!candidate.getEmployer()
                .getId()
                .equals(interview.getEmployer().getId())) {

            throw new IllegalArgumentException(
                    "Candidate does not belong to this interview."
            );
        }


        /*
         * Make sure the candidate belongs to
         * this interview.
         */

        if (!candidate.getInterview()
                .equals(interview.getTitle())) {

            throw new IllegalArgumentException(
                    "Candidate does not belong to this interview."
            );
        }


        /*
         * Find the interview question.
         */

        List<Question> questions =
                questionRepository
                        .findAllByInterviewIdOrderByQuestionOrderAsc(
                                interview.getId()
                        );

        Question question =
                questions.stream()
                        .filter(q ->
                                q.getQuestionOrder()
                                        .equals(questionNumber)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interview question not found."
                                )
                        );


        /*
         * Create upload directory.
         */

        try {

            Path uploadDirectory =
                    Paths.get(
                            "uploads",
                            "candidate-responses"
                    );

            Files.createDirectories(
                    uploadDirectory
            );


            /*
             * Determine file extension.
             */

            String originalFilename =
                    video.getOriginalFilename();

            String extension = ".webm";

            if (originalFilename != null &&
                    originalFilename.contains(".")) {

                extension =
                        originalFilename.substring(
                                originalFilename.lastIndexOf(".")
                        );
            }


            /*
             * Generate unique filename.
             */

            String filename =
                    UUID.randomUUID()
                            .toString()
                            + extension;

            Path filePath =
                    uploadDirectory.resolve(filename);


            /*
             * Save video.
             */

            Files.copy(
                    video.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            /*
             * URL stored in PostgreSQL.
             */

            String videoUrl =
                    "/uploads/candidate-responses/"
                            + filename;


            /*
             * Find existing response or create one.
             */

            CandidateResponse response =
                    candidateResponseRepository
                            .findByCandidateIdAndQuestionNumber(
                                    candidateId,
                                    questionNumber
                            )
                            .orElseGet(
                                    CandidateResponse::new
                            );


            response.setCandidate(candidate);

            response.setQuestionNumber(
                    question.getQuestionOrder()
            );

            response.setQuestion(
                    question.getQuestionText()
            );

            response.setVideoUrl(
                    videoUrl
            );

            response.setSubmittedAt(
                    LocalDateTime.now()
            );


            /*
             * Save response.
             */

            candidateResponseRepository.save(
                    response
            );


            /*
             * Return updated candidate.
             */

            return toDto(candidate);

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "Failed to save video response."
            );
        }
    }


    /*
     * ============================================================
     * GET ALL CANDIDATES
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<CandidateDto> getCandidates(
            Long employerId
    ) {

        return candidateRepository
                .findAllByEmployerIdOrderByIdDesc(employerId)
                .stream()
                .map(this::toDto)
                .toList();
    }


    /*
     * ============================================================
     * GET ONE CANDIDATE
     * ============================================================
     */

    @Transactional(readOnly = true)
    public CandidateDto getCandidate(
            Long employerId,
            Long candidateId
    ) {

        Candidate candidate =
                candidateRepository
                        .findByIdAndEmployerId(
                                candidateId,
                                employerId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found."
                                )
                        );

        return toDto(candidate);
    }


    /*
     * ============================================================
     * CREATE PUBLIC CANDIDATE
     * ============================================================
     */

    @Transactional
    public CandidateDto createPublicCandidate(
            String interviewToken,
            String name,
            String email
    ) {

        if (name == null ||
                name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        if (email == null ||
                email.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Email address is required."
            );
        }


        Interview interview =
                interviewRepository
                        .findByInterviewToken(interviewToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interview link is invalid or expired."
                                )
                        );


        /*
         * Check whether this candidate already
         * exists for this interview.
         */

        List<Candidate> existingCandidates =
                candidateRepository
                        .findAllByEmployerIdOrderByIdDesc(
                                interview.getEmployer().getId()
                        );

        Candidate existingCandidate =
                existingCandidates
                        .stream()
                        .filter(candidate ->
                                candidate.getEmail()
                                        .equalsIgnoreCase(
                                                email.trim()
                                        )
                                        &&
                                candidate.getInterview()
                                        .equals(
                                                interview.getTitle()
                                        )
                        )
                        .findFirst()
                        .orElse(null);


        if (existingCandidate != null) {
            return toDto(existingCandidate);
        }


        /*
         * Create candidate.
         */

        Candidate candidate =
                new Candidate();

        candidate.setEmployer(
                interview.getEmployer()
        );

        candidate.setName(
                name.trim()
        );

        candidate.setEmail(
                email.trim()
        );

        candidate.setPosition(
                interview.getPosition()
        );

        candidate.setInterview(
                interview.getTitle()
        );

        candidate.setDepartment(
                interview.getDepartment()
        );

        candidate.setLocation(
                interview.getLocation()
        );

        candidate.setStatus(
                "Pending"
        );

        candidate.setSubmittedAt(
                null
        );


        Candidate savedCandidate =
                candidateRepository.save(
                        candidate
                );

        return toDto(savedCandidate);
    }


    /*
     * ============================================================
     * SUBMIT INTERVIEW
     * ============================================================
     */

    @Transactional
    public CandidateDto submitInterview(
            Long candidateId,
            Map<String, Object> answers
    ) {

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found."
                                )
                        );


        /*
         * Find interview.
         */

        Interview interview =
                interviewRepository
                        .findByTitleAndEmployerId(
                                candidate.getInterview(),
                                candidate.getEmployer().getId()
                        )
                        .orElse(null);


        /*
         * Get questions once.
         */

        List<Question> questions =
                interview != null
                        ? questionRepository
                            .findAllByInterviewIdOrderByQuestionOrderAsc(
                                    interview.getId()
                            )
                        : List.of();


        /*
         * Save candidate answers.
         */

        if (answers != null) {

            for (Map.Entry<String, Object> entry :
                    answers.entrySet()) {

                try {

                    int questionIndex =
                            Integer.parseInt(
                                    entry.getKey()
                            );

                    Object answerValue =
                            entry.getValue();

                    String answer =
                            answerValue == null
                                    ? ""
                                    : answerValue.toString();


                    Question question = null;

                    if (questionIndex >= 0 &&
                            questionIndex < questions.size()) {

                        question =
                                questions.get(
                                        questionIndex
                                );
                    }


                    int questionNumber =
                            question != null
                                    ? question.getQuestionOrder()
                                    : questionIndex + 1;


                    /*
                     * Find existing response.
                     *
                     * This prevents duplicate responses
                     * if the candidate uploads a video first
                     * and submits the interview afterwards.
                     */

                    CandidateResponse response =
                            candidateResponseRepository
                                    .findByCandidateIdAndQuestionNumber(
                                            candidateId,
                                            questionNumber
                                    )
                                    .orElseGet(
                                            CandidateResponse::new
                                    );


                    response.setCandidate(
                            candidate
                    );

                    response.setQuestionNumber(
                            questionNumber
                    );

                    response.setQuestion(
                            question != null
                                    ? question.getQuestionText()
                                    : "Question " +
                                      questionNumber
                    );


                    /*
                     * Save the actual candidate answer.
                     */

                    response.setAnswer(
                            answer
                    );


                    /*
                     * IMPORTANT:
                     *
                     * Do not clear videoUrl here.
                     *
                     * A video may already have been uploaded
                     * before the candidate submits the interview.
                     */

                    if (response.getVideoUrl() == null) {
                        response.setVideoUrl(null);
                    }


                    response.setSubmittedAt(
                            LocalDateTime.now()
                    );


                    candidateResponseRepository.save(
                            response
                    );

                } catch (NumberFormatException ignored) {

                    /*
                     * Ignore invalid question keys.
                     */

                }
            }
        }


        /*
         * Mark candidate as completed.
         */

        candidate.setStatus(
                "Completed"
        );

        candidate.setSubmittedAt(
                LocalDateTime.now()
        );

        candidateRepository.save(
                candidate
        );
               notificationService.createForCandidate(
        NotificationType.CANDIDATE_SUBMITTED,
        "New Candidate Submission",
        "A candidate has submitted their interview responses and is ready for review.",
        candidate,
        interview != null ? interview.getId() : null
);

        return toDto(candidate);
    }


    /*
     * ============================================================
     * CONVERT CANDIDATE TO DTO
     * ============================================================
     */

    private CandidateDto toDto(
            Candidate candidate
    ) {

        List<CandidateResponseDto> responses =
                candidate.getResponses()
                        .stream()
                        .map(this::toResponseDto)
                        .toList();


        return new CandidateDto(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                candidate.getPosition(),
                candidate.getInterview(),
                candidate.getDepartment(),
                candidate.getLocation(),
                candidate.getStatus(),

                candidate.getSubmittedAt() == null
                        ? "-"
                        : DATE_FORMAT.format(
                                candidate.getSubmittedAt()
                        ),

                responses
        );
    }


    /*
     * ============================================================
     * CONVERT RESPONSE TO DTO
     * ============================================================
     */

    private CandidateResponseDto toResponseDto(
            CandidateResponse response
    ) {

        return new CandidateResponseDto(
                response.getId(),
                response.getQuestionNumber(),
                response.getQuestion(),
                response.getAnswer(),
                response.getVideoUrl(),
                response.getDuration(),

                response.getSubmittedAt() == null
                        ? "-"
                        : DATE_TIME_FORMAT.format(
                                response.getSubmittedAt()
                        )
        );
    }
}