package com.external.imomarkastore.service.impl;

import com.external.imomarkastore.exception.BusinessLogicException;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.repository.ApplicationRepository;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
    private final ClientInfoService clientInfoService;
    private final BotMessageSource messageSource;

    @Override
    public Application create(Long telegramUserId, String phoneNumber) {
        final var application = new Application();
        application.setTelegramUserId(telegramUserId);
        application.setPhoneNumber(phoneNumber);
        application.setStatus(CREATION_IN_PROGRESS);
        application.setSentRequestForPayment(false);
        application.setPaid(false);
        return repository.save(application);
    }

    @Override
    public Application getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessLogicException("Could not find application by id: %s".formatted(id)));
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
    public List<Application> getFullyCreatedApplications() {
        return repository.findByStatusOrderByCreatedAtDesc(FULLY_CREATED);
    }

    @Override
    public List<Application> getArchivedApplications() {
        return repository.findByStatusOrderByCreatedAtDesc(ARCHIVED);
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
    public Application getFirstInProgressByTelegramUserId(Long telegramUserId) {
        return repository.findTopByTelegramUserIdAndStatusOrderByCreatedAtDesc(
                        telegramUserId, CREATION_IN_PROGRESS)
                .orElseThrow(() -> new BusinessLogicException("Could not find application in status 'creation in progress' for this user"));
    }

    public String getApplicationPayloadForClient(Application application) {
        final var carDetails = carDetailsService.getById(application.getCarDetailsId());
        final var details = Optional.ofNullable(carDetails.getDetails())
                .orElse(EMPTY);
        final var vinNumber = Optional.ofNullable(carDetails.getVinNumber())
                .orElse(EMPTY);
        return messageSource.getMessage("template.client.application",
                Stream.of(
                                application.getId().toString(),
                                details,
                                vinNumber,
                                application.getMainPurpose(),
                                application.getComment(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(application.getCreatedAt()),
                                application.isPaid() ? "+" : "-")
                        .map(string -> isBlank(string) ? EMPTY : string)
                        .toArray());
    }

    @Override
    public String getApplicationPayloadForOwner(Application application) {
        final var carDetails = carDetailsService.getById(application.getCarDetailsId());
        final var details = Optional.ofNullable(carDetails.getDetails())
                .orElse(EMPTY);
        final var vinNumber = Optional.ofNullable(carDetails.getVinNumber())
                .orElse(EMPTY);
        final var clientInfo = clientInfoService.getByTelegramUserId(application.getTelegramUserId());
        final var clientName = Optional.ofNullable(clientInfo.getName()).orElse(EMPTY);
        final var clientPhoneNumber = Optional.ofNullable(clientInfo.getPhoneNumber()).orElse(EMPTY);
        final var clientTelegramNickname = Optional.ofNullable(clientInfo.getTelegramUserName()).orElse(EMPTY);
        return messageSource.getMessage("template.owner.application",
                Stream.of(
                                application.getId().toString(),
                                details,
                                vinNumber,
                                application.getMainPurpose(),
                                application.getComment(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(application.getCreatedAt()),
                                clientName,
                                clientPhoneNumber,
                                clientTelegramNickname,
                                application.isPaid() ? "+" : "-")
                        .map(string -> isBlank(string) ? EMPTY : string).toArray());
    }

    @Override
    public void updateAll(List<Application> applications) {
        repository.saveAll(applications);
    }

    @Override
    public List<Application> getNotArchivedApplicationsForCar(CarDetails carDetails) {
        return repository.findByStatusNotAndCarDetailsIdOrderById(ARCHIVED, carDetails.getId());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
