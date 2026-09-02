package com.covira.backend.service;

import com.covira.backend.dto.NotificationDto;
import com.covira.backend.entity.Candidate;
import com.covira.backend.entity.Interview;
import com.covira.backend.entity.Notification;
import com.covira.backend.entity.User;
import com.covira.backend.repository.NotificationRepository;
import com.covira.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class NotificationService {

    private static final int MAX_NOTIFICATIONS_RETURNED = 30;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

   
    public NotificationDto create(
            Long employerId,
            String type,
            String title,
            String message,
            Long candidateId,
            String candidateName,
            Long interviewId,
            String interviewTitle
    ) {

        User employer = userRepository.findById(employerId)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found."));

        Notification notification = new Notification();
        notification.setEmployer(employer);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCandidateId(candidateId);
        notification.setCandidateName(candidateName);
        notification.setInterviewId(interviewId);
        notification.setInterviewTitle(interviewTitle);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        return toDto(saved);
    }

    /*
     * ============================================================
     * CONVENIENCE: CANDIDATE-RELATED NOTIFICATION
     * ============================================================
     *
     * For any event tied to a specific candidate (submitted,
     * video submitted, shortlisted, rejected, awaiting review,
     * etc). Pulls the employer id straight off the candidate so
     * call sites don't have to.
     *
     * `interviewId` is accepted explicitly (rather than looked up
     * here) because Candidate only stores the interview *title*
     * as a string, not a foreign key - the caller already has the
     * resolved Interview entity in most cases (e.g. submitInterview),
     * so this avoids a duplicate lookup. Pass null if unknown.
     */
    public NotificationDto createForCandidate(
            String type,
            String title,
            String message,
            Candidate candidate,
            Long interviewId
    ) {

        return create(
                candidate.getEmployer().getId(),
                type,
                title,
                message,
                candidate.getId(),
                candidate.getName(),
                interviewId,
                candidate.getInterview()
        );
    }

    public NotificationDto createForInterview(
            String type,
            String title,
            String message,
            Interview interview
    ) {

        return create(
                interview.getEmployer().getId(),
                type,
                title,
                message,
                null,
                null,
                interview.getId(),
                interview.getTitle()
        );
    }

    public List<NotificationDto> getNotifications(Long employerId) {

        return notificationRepository
                .findAllByEmployerIdOrderByCreatedAtDesc(employerId)
                .stream()
                .limit(MAX_NOTIFICATIONS_RETURNED)
                .map(this::toDto)
                .toList();
    }

    public long getUnreadCount(Long employerId) {
        return notificationRepository.countByEmployerIdAndIsReadFalse(employerId);
    }

    public NotificationDto markAsRead(Long employerId, Long notificationId) {

        Notification notification = notificationRepository
                .findByIdAndEmployerId(notificationId, employerId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));

        notification.setRead(true);

        notificationRepository.save(notification);

        return toDto(notification);
    }

    private NotificationDto toDto(Notification notification) {

        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCandidateId(),
                notification.getCandidateName(),
                notification.getInterviewId(),
                notification.getInterviewTitle(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}