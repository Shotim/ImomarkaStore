package com.external.imomarkastore.telegramapi.message.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.ClientState.CHOOSE_CAR_FOR_APPLICATION;
import static com.external.imomarkastore.constant.ClientState.CREATE_APPLICATION;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithInlineButton;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    @SneakyThrows
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) {
        clientInfo.setState(CREATE_APPLICATION);
        clientInfoService.update(clientInfo);
        applicationService.create(clientInfo.getTelegramUserId(), clientInfo.getPhoneNumber());
        sendMessages(update, clientInfo);
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var user = getUserFromUpdate(update);
        final var text = messageSource.getMessage("insertCarDetails");
        final var message = createTextMessageForUserWithRemoveKeyBoard(user, text);
        inomarkaStore.execute(message);
        final var carDetailsForClient = carDetailsService.getActiveCarDetailsForClient(clientInfo);
        if (!carDetailsForClient.isEmpty()) {
            final var chooseCarText = messageSource.getMessage("chooseSavedCar");
            final var chooseCarMessage = createTextMessageForUserWithRemoveKeyBoard(user, chooseCarText);
            final var rootMainMessage = inomarkaStore.execute(chooseCarMessage).getMessageId();
            final var messageIds = new JsonArray();
            messageIds.add(rootMainMessage);
            for (CarDetails carDetails : carDetailsForClient) {
                final var carDetailsText = carDetailsService.getCarDetailsPayload(carDetails);
                final var messageWithInlineButton = createTextMessageWithInlineButton
                        (user, carDetailsText,
                                messageSource.getMessage("buttonName.chooseCarForApplication"),
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
