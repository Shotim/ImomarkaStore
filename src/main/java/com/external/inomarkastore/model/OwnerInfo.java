package com.external.inomarkastore.model;

import com.external.inomarkastore.constant.OwnerState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import static jakarta.persistence.EnumType.STRING;

@Data
@Entity
@Table(name = "owner_info")
public class OwnerInfo {
    @Id
    private Long telegramUserId;
    @Enumerated(STRING)
    private OwnerState ownerState;
    @Column(length = 8192)
    private String jsonData;
    private String ownerName;
    private String phoneNumber;
    private String address;
    private String email;
    private String inn;
    private String workingHours;
}
