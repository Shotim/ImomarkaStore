package com.external.imomarkastore.service;

import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationService {

    Application create(Long telegramUserId, String phoneNumber);

    Optional<Application> getById(UUID id);

    Application update(Application application);

    List<Application> getFullyCreatedApplicationsForClient(ClientInfo clientInfo);
    List<Application> getFullyCreatedApplications();

    List<Application> getArchivedApplicationsForClient(ClientInfo clientInfo);

    List<Application> getApplicationsForClient(ClientInfo clientInfo);

    Application findFirstInProgressByTelegramUserId(Long telegramUserId);

    String getApplicationPayloadForClient(Application application);
    String getApplicationPayloadForOwner(Application application);

    List<Application> updateAll(List<Application> applications);

    List<Application> getNotArchivedApplicationsForCar(CarDetails carDetails);
}
