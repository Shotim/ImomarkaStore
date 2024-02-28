package com.external.inomarkastore.service;

import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.model.CarDetails;
import com.external.inomarkastore.model.ClientInfo;

import java.util.List;

public interface ApplicationService {
    Application create(Long telegramUserId, String phoneNumber);

    Application create(String phoneNumber);

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
