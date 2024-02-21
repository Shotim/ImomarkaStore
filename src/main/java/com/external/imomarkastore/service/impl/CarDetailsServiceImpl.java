package com.external.imomarkastore.service.impl;

import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.repository.CarDetailsRepository;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.external.imomarkastore.constant.CarState.ACTIVE;
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
    public Optional<CarDetails> getById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<CarDetails> getActiveCarDetailsForClient(ClientInfo clientInfo) {
        return repository.findByTelegramUserIdOrPhoneNumberAndCarState(
                clientInfo.getTelegramUserId(), clientInfo.getPhoneNumber(), ACTIVE);
    }

    @Override
    public List<CarDetails> updateAll(List<CarDetails> carDetailsList) {
        return repository.saveAll(carDetailsList);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public String getCarDetailsPayload(CarDetails carDetails) {
        return messageSource.getMessage("template.client.carDetails",
                Stream.of(carDetails.getDetails(), carDetails.getVinNumber())
                        .map(string -> isBlank(string) ? EMPTY : string).toArray());
    }
}
