package com.external.inomarkastore.repository;

import com.external.inomarkastore.constant.ApplicationStatus;
import com.external.inomarkastore.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @Query("SELECT a FROM Application a WHERE a.status = ?1 and (a.telegramUserId = ?2 OR a.phoneNumber = ?3) ORDER BY a.id")
    List<Application> findByStatusAndTelegramUserIdOrPhoneNumber(ApplicationStatus status, Long telegramUserId, String phoneNumber);
    List<Application> findByStatusAndPhoneNumber(ApplicationStatus status, String phoneNumber);

    List<Application> findByTelegramUserIdOrPhoneNumberOrderById(Long telegramUserId, String phoneNumber);

    Optional<Application> findTopByTelegramUserIdAndStatusOrderByCreatedAtDesc(Long telegramUserId, ApplicationStatus status);

    List<Application> findByStatusNotAndCarDetailsIdOrderById(ApplicationStatus status, UUID carDetailsId);

    List<Application> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);
}
