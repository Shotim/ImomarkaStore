package com.external.imomarkastore.service;

import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;

import java.util.List;

public interface ApplicationService {
    Application create(Long telegramUserId, String phoneNumber);

    Application getById(Long id);

    Application update(Application application);

    List<Application> getFullyCreatedApplicationsForClient(ClientInfo clientInfo);

    List<Application> getFullyCreatedApplications();

    List<Application> getArchivedApplications();

    List<Application> getArchivedApplicationsForClient(ClientInfo clientInfo);

    List<Application> getApplicationsForClient(ClientInfo clientInfo);

    Application getFirstInProgressByTelegramUserId(Long telegramUserId);

    String getApplicationPayloadForClient(Application application);

    String getApplicationPayloadForOwner(Application application);

    void updateAll(List<Application> applications);

    List<Application> getNotArchivedApplicationsForCar(CarDetails carDetails);

    void deleteById(Long id);
}
