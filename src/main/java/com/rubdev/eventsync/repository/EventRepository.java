package com.rubdev.eventsync.repository;

import com.rubdev.eventsync.model.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Optional<EventEntity> findByBasePlanIdAndStartDateAndEndDate(Long basePlanId, LocalDateTime startDate, LocalDateTime endDate);
}
