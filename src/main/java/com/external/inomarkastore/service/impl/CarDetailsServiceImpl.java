package com.external.inomarkastore.service.impl;

import com.external.inomarkastore.exception.BusinessLogicException;
import com.external.inomarkastore.model.CarDetails;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.repository.CarDetailsRepository;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.external.inomarkastore.constant.CarState.ACTIVE;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class CarDetailsServiceImpl implements CarDetailsService {

    private final CarDetailsRepository repository;
    private final BotMessageSource messageSource;

    @Override
    public CarDetails create() {
        final var carDetails = new CarDetails();
        carDetails.setId(randomUUID());
        carDetails.setCarState(ACTIVE);
        return repository.save(carDetails);
    }

    @Override
    public CarDetails update(CarDetails carDetails) {
        return repository.save(carDetails);
    }

    @Override
    public CarDetails getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessLogicException("Could not find car by id: %s".formatted(id)));
    }

    @Override
    public List<CarDetails> getActiveCarDetailsForClient(ClientInfo clientInfo) {
        return repository.findByTelegramUserIdOrPhoneNumberAndCarState(
                clientInfo.getTelegramUserId(), clientInfo.getPhoneNumber(), ACTIVE);
    }

    @Override
    public void updateAll(List<CarDetails> carDetailsList) {
        repository.saveAll(carDetailsList);
    }

    @Override
    public String getCarDetailsPayload(CarDetails carDetails) {
        return messageSource.getMessage("template.client.carDetails",
                Stream.of(carDetails.getDetails(), carDetails.getVinNumber())
                        .map(string -> isBlank(string) ? EMPTY : string).toArray());
    }
}
