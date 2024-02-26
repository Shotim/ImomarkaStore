package com.external.inomarkastore.model;

import com.external.inomarkastore.constant.CarState;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import static com.external.inomarkastore.util.StringDbFieldUtils.scaleString;
import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "car_details")
@NoArgsConstructor
@Getter
public class CarDetails {
    @Id
    @Setter
    private UUID id;
    private String details;
    @Setter
    private String vinNumber;
    @Setter
    String vinNumberPhotoId;
    @Setter
    private Long telegramUserId;
    @Setter
    private String phoneNumber;
    @Setter
    @Enumerated(STRING)
    private CarState carState;

    public void setDetails(String carDetails) {
        this.details = scaleString(carDetails);
    }
}
