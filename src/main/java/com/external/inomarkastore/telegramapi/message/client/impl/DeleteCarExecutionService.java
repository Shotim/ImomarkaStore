package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.UUID;

import static com.external.inomarkastore.constant.CarState.ARCHIVED;
import static com.external.inomarkastore.constant.ClientState.DELETE_CAR;
import static com.external.inomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.inomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUUIDIdFromCallbackDataFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class DeleteCarExecutionService implements MessageExecutionService {
    private final CarDetailsService carDetailsService;
    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return DELETE_CAR;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final UUID uuid = getUUIDIdFromCallbackDataFromUpdate(update);
        final var carDetails = carDetailsService.getById(uuid);
        carDetails.setCarState(ARCHIVED);
        carDetailsService.update(carDetails);
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {

        final var text = messageSource.getMessage("carDeleted");
        final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
        final var callbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, text);
        inomarkaStore.execute(callbackQuery);

        final var id = getUUIDIdFromCallbackDataFromUpdate(update);
        final var jsonObject = new Gson()
                .fromJson(clientInfo.getAdditionalJsonDataForNextOperations(), JsonObject.class);
        clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
        clientInfoService.update(clientInfo);

        final var messageId = jsonObject.remove(id.toString()).getAsInt();
        final var user = getUserFromUpdate(update);

        final var deleteMessage = createDeleteMessageForUser(user.getId(), messageId);
        inomarkaStore.execute(deleteMessage);
    }
}