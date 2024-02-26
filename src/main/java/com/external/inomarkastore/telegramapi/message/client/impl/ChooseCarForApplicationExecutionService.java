package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.constant.ClientState.CHOOSE_CAR_FOR_APPLICATION;
import static com.external.inomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.inomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUUIDIdFromCallbackDataFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class ChooseCarForApplicationExecutionService implements MessageExecutionService {

    private final ApplicationService applicationService;
    private final ClientInfoService clientInfoService;
    private final CarDetailsService carDetailsService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return CHOOSE_CAR_FOR_APPLICATION;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var carDetailsId = getUUIDIdFromCallbackDataFromUpdate(update);
        final var user = getUserFromUpdate(update);
        final var application = applicationService.getFirstInProgressByTelegramUserId(user.getId());
        application.setCarDetailsId(carDetailsId);
        applicationService.update(application);
        clientInfo.setState(CHOOSE_CAR_FOR_APPLICATION);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var carDetailsId = getUUIDIdFromCallbackDataFromUpdate(update);
        final var carDetails = carDetailsService.getById(carDetailsId);
        final var details = isBlank(carDetails.getDetails()) ? EMPTY : carDetails.getDetails();
        final var chooseCarMessageText = messageSource
                .getMessage("youChooseThatCar", List.of(details).toArray());

        final var chooseCarMessage = createTextMessageForUser(user.getId(), chooseCarMessageText);
        inomarkaStore.execute(chooseCarMessage);

        final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
        final var callbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, chooseCarMessageText);
        inomarkaStore.execute(callbackQuery);
        final var jsonString = clientInfo.getAdditionalJsonDataForNextOperations();
        final var jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
        final var messageIds = jsonObject.get("messageIds")
                .getAsJsonArray()
                .asList().stream()
                .map(JsonElement::getAsInt)
                .toList();
        for (Integer messageId : messageIds) {
            final var deleteMessage = createDeleteMessageForUser(user.getId(), messageId);
            inomarkaStore.execute(deleteMessage);
        }
        clientInfo.setAdditionalJsonDataForNextOperations(null);
        clientInfoService.update(clientInfo);
        final var text = messageSource.getMessage("describeMainPurpose");
        final var message = createTextMessageForUser(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
