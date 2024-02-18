package com.external.imomarkastore.model;

import com.external.imomarkastore.constant.ClientState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import static com.external.imomarkastore.util.StringDbFieldUtils.scaleString;
import static jakarta.persistence.EnumType.STRING;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "client_info")
public class ClientInfo {
    @Id
    @Setter
    private UUID id;
    private String name;
    private String phoneNumber;
    @Setter
    private Long telegramUserId;
    @Enumerated(STRING)
    @Setter
    private ClientState state;
    @Setter
    @Column(length = 8192)
    private String additionalJsonDataForNextOperations;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = scaleString(phoneNumber);
    }

    public void setName(String name) {
        this.name = scaleString(name);
    }
}
