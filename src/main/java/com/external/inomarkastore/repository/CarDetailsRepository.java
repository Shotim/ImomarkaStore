package com.external.inomarkastore.repository;

import com.external.inomarkastore.constant.CarState;
import com.external.inomarkastore.model.CarDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CarDetailsRepository extends JpaRepository<CarDetails, UUID> {
    @Query("SELECT car FROM CarDetails car WHERE (car.telegramUserId=?1 or car.phoneNumber=?2) AND car.carState=?3")
    List<CarDetails> findByTelegramUserIdOrPhoneNumberAndCarState(Long telegramUserId, String phoneNumber, CarState carState);

    List<CarDetails> findByPhoneNumberAndCarState(String phoneNumber, CarState carState);
}
