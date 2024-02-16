package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.external.imomarkastore.constant.ClientState.CHOOSE_CAR_FOR_APPLICATION;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getIdFromCallbackData;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
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
    public void execute(Update update, ClientInfo clientInfo) {
        final var carDetailsId = getIdFromCallbackData(update);
        final var user = getUserFromUpdate(update);
        final var application = applicationService.findFirstInProgressByTelegramUserId(user.getId());
        application.setCarDetailsId(carDetailsId);
        applicationService.update(application);
        clientInfo.setState(CHOOSE_CAR_FOR_APPLICATION);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var user = getUserFromUpdate(update);
        final var carDetailsId = getIdFromCallbackData(update);
        final var carDetailsOptional = carDetailsService.getById(carDetailsId);
        if (carDetailsOptional.isPresent()) {
            final var carDetails = carDetailsOptional.get();
            final var details = isBlank(carDetails.getDetails()) ? EMPTY : carDetails.getDetails();
            final var chooseCarMessageText = messageSource
                    .getMessage("youChooseThatCar", List.of(details).toArray());

            final var chooseCarMessage = createTextMessageForUser(user, chooseCarMessageText);
            inomarkaStore.execute(chooseCarMessage);

            final var callbackQuery = AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .text(chooseCarMessageText)
                    .build();
            inomarkaStore.execute(callbackQuery);
        }
        final var jsonString = clientInfo.getAdditionalJsonDataForNextOperations();
        final var jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
        final var messageIds = jsonObject.get("messageIds")
                .getAsJsonArray()
                .asList().stream()
                .map(JsonElement::getAsInt)
                .toList();
        for (Integer messageId : messageIds) {
            final var deleteMessage = DeleteMessage.builder()
                    .chatId(user.getId())
                    .messageId(messageId)
                    .build();
            inomarkaStore.execute(deleteMessage);
        }
        clientInfo.setAdditionalJsonDataForNextOperations(null);
        clientInfoService.update(clientInfo);
        final var text = messageSource.getMessage("describeMainPurpose");
        final var message = createTextMessageForUser(user, text);
        inomarkaStore.execute(message);
    }
}
