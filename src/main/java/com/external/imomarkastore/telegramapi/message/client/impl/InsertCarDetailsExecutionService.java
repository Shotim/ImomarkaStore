package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.ClientState.INSERT_CAR_DETAILS;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class InsertCarDetailsExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final CarDetailsService carDetailsService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return INSERT_CAR_DETAILS;
    }

    @Override
    @SneakyThrows
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) {
        clientInfo.setState(INSERT_CAR_DETAILS);
        clientInfoService.update(clientInfo);
        final var application = applicationService.getFirstInProgressByTelegramUserId(clientInfo.getTelegramUserId());
        final var text = getTextFromUpdate(update);
        final var carDetails = carDetailsService.create();
        application.setCarDetailsId(carDetails.getId());
        applicationService.update(application);
        carDetails.setDetails(text);
        carDetails.setPhoneNumber(clientInfo.getPhoneNumber());
        carDetails.setTelegramUserId(clientInfo.getTelegramUserId());
        carDetailsService.update(carDetails);
        sendMessages(update, clientInfo);
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var text = messageSource.getMessage("insertVinNumber");
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUser(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
