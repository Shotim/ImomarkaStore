package com.external.imomarkastore.model;

import com.external.imomarkastore.constant.ApplicationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.external.imomarkastore.util.StringDbFieldUtils.scaleString;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "application")
public class Application {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter
    private Long id;
    private String mainPurpose;
    @Setter
    private String mainPurposePhotoId;
    private String comment;
    @Setter
    private Long telegramUserId;
    private String phoneNumber;
    @Setter
    private UUID carDetailsId;
    @Enumerated(STRING)
    @Setter
    private ApplicationStatus status;
    @CreationTimestamp
    @Setter
    private LocalDateTime createdAt;

    public void setMainPurpose(String mainPurpose) {
        this.mainPurpose = scaleString(mainPurpose);
    }

    public void setComment(String comment) {
        this.comment = scaleString(comment);
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = scaleString(phoneNumber);
    }
}
