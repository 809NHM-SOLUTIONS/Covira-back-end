package com.covira.backend.repository;

import com.covira.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByEmployerIdOrderByCreatedAtDesc(Long employerId);

    long countByEmployerIdAndIsReadFalse(Long employerId);

    Optional<Notification> findByIdAndEmployerId(Long id, Long employerId);
}