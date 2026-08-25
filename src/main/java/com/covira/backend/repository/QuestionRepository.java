package com.covira.backend.repository;

import com.covira.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByUserEmail(String userEmail);

    Optional<Question> findByIdAndUserEmail(
            Long id,
            String userEmail
    );

    List<Question> findAllByIdInAndUserEmail(
            List<Long> ids,
            String userEmail
    );
}