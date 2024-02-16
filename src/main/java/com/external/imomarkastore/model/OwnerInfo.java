package com.external.imomarkastore.model;

import com.external.imomarkastore.constant.OwnerState;
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
}
