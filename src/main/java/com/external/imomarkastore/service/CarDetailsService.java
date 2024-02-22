package com.external.imomarkastore.service;

import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;

import java.util.List;
import java.util.UUID;

public interface CarDetailsService {
    CarDetails create();

    CarDetails update(CarDetails carDetails);

    CarDetails getById(UUID id);

    List<CarDetails> getActiveCarDetailsForClient(ClientInfo clientInfo);

    void updateAll(List<CarDetails> carDetailsList);

    String getCarDetailsPayload(CarDetails carDetails);
}
