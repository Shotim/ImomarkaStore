package com.external.imomarkastore.service.impl;

import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.repository.ApplicationRepository;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.external.imomarkastore.constant.ApplicationStatus.ARCHIVED;
import static com.external.imomarkastore.constant.ApplicationStatus.CREATION_IN_PROGRESS;
import static com.external.imomarkastore.constant.ApplicationStatus.FULLY_CREATED;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository repository;
    private final CarDetailsService carDetailsService;
    private final BotMessageSource messageSource;

    @Override
    public Application create(Long telegramUserId, String phoneNumber) {
        final var application = new Application();
        application.setTelegramUserId(telegramUserId);
        application.setPhoneNumber(phoneNumber);
        application.setStatus(CREATION_IN_PROGRESS);
        return repository.save(application);
    }

    @Override
    public Optional<Application> getById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Application update(Application application) {
        return repository.save(application);
    }

    @Override
    public List<Application> getFullyCreatedApplicationsForClient(ClientInfo clientInfo) {
        return repository.findByStatusAndTelegramUserIdOrPhoneNumber(
                FULLY_CREATED, clientInfo.getTelegramUserId(), clientInfo.getPhoneNumber());
    }

    @Override
    public List<Application> getArchivedApplicationsForClient(ClientInfo clientInfo) {

        return repository.findByStatusAndTelegramUserIdOrPhoneNumber(
                ARCHIVED, clientInfo.getTelegramUserId(), clientInfo.getPhoneNumber());
    }

    @Override
    public List<Application> getApplicationsForClient(ClientInfo clientInfo) {
        return repository.findByTelegramUserIdOrPhoneNumberOrderById(
                clientInfo.getTelegramUserId(), clientInfo.getPhoneNumber());
    }

    @Override
    public Application findFirstInProgressByTelegramUserId(Long telegramUserId) {
        return repository.findTopByTelegramUserIdAndStatusOrderByCreatedAtDesc(
                telegramUserId, CREATION_IN_PROGRESS).orElseThrow(IllegalArgumentException::new);
    }

    public String getApplicationPayload(Application application) {
        final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
        final var carDetails = carDetailsOptional
                .map(CarDetails::getDetails).orElse(EMPTY);
        final var vinNumber = carDetailsOptional.map(CarDetails::getVinNumber).orElse(EMPTY);
        return messageSource.getMessage("template.application", Stream.of(
                        application.getId().toString(),
                        carDetails,
                        vinNumber,
                        application.getMainPurpose(),
                        application.getComment(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(application.getCreatedAt()))
                .map(string -> isBlank(string) ? EMPTY : string)
                .toArray());
    }

    @Override
    public List<Application> updateAll(List<Application> applications) {
        return repository.saveAll(applications);
    }

    @Override
    public List<Application> getNotArchivedApplicationsForCar(CarDetails carDetails) {
        return repository.findByStatusNotAndCarDetailsIdOrderById(ARCHIVED, carDetails.getId());
    }
}
