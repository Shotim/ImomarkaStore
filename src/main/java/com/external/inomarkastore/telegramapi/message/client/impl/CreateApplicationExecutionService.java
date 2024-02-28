package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.CarDetails;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.ClientState.CHOOSE_CAR_FOR_APPLICATION;
import static com.external.inomarkastore.constant.ClientState.CREATE_APPLICATION;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButton;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class CreateApplicationExecutionService implements MessageExecutionService {

    private final InomarkaStore inomarkaStore;
    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final CarDetailsService carDetailsService;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return CREATE_APPLICATION;
    }

    @Override

    @Transactional
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        clientInfo.setState(CREATE_APPLICATION);
        clientInfoService.update(clientInfo);
        applicationService.create(clientInfo.getTelegramUserId(), clientInfo.getPhoneNumber());
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var text = messageSource.getMessage("insertCarDetails");
        final var message = createTextMessageForUserWithRemoveKeyBoard(user.getId(), text);
        inomarkaStore.execute(message);
        final var carDetailsForClient = carDetailsService.getActiveCarDetailsForClient(clientInfo);
        if (!carDetailsForClient.isEmpty()) {
            final var chooseCarText = messageSource.getMessage("chooseSavedCar");
            final var chooseCarMessage = createTextMessageForUserWithRemoveKeyBoard(user.getId(), chooseCarText);
            final var rootMainMessage = inomarkaStore.execute(chooseCarMessage).getMessageId();
            final var messageIds = new JsonArray();
            messageIds.add(rootMainMessage);
            for (CarDetails carDetails : carDetailsForClient) {
                final var carDetailsText = carDetailsService.getCarDetailsPayload(carDetails);
                final var messageWithInlineButton = createTextMessageForUserWithInlineButton
                        (user.getId(), carDetailsText,
                                messageSource.getMessage("buttonName.client.chooseCarForApplication"),
                                "%s:%s".formatted(CHOOSE_CAR_FOR_APPLICATION.name(), carDetails.getId()));
                final var executed = inomarkaStore.execute(messageWithInlineButton);
                messageIds.add(executed.getMessageId());
            }
            final var jsonObject = new JsonObject();
            jsonObject.add("messageIds", messageIds);
            clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
            clientInfoService.update(clientInfo);
        }
    }
}