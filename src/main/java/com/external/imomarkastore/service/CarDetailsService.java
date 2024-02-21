package com.external.imomarkastore.service;

import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarDetailsService {
    CarDetails create();

    CarDetails update(CarDetails carDetails);

    Optional<CarDetails> getById(UUID id);

    List<CarDetails> getActiveCarDetailsForClient(ClientInfo clientInfo);

    List<CarDetails> updateAll(List<CarDetails> carDetailsList);

    void deleteById(UUID id);

    String getCarDetailsPayload(CarDetails carDetails);
}
