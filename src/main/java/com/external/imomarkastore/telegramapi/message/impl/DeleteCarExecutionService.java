package com.external.imomarkastore.telegramapi.message.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.UUID;

import static com.external.imomarkastore.constant.CarState.ARCHIVED;
import static com.external.imomarkastore.constant.ClientState.DELETE_CAR;
import static com.external.imomarkastore.util.UpdateUtils.getIdFromCallbackData;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    public void execute(Update update, ClientInfo clientInfo) {
        final UUID uuid = getIdFromCallbackData(update);
        final var carDetailsOptional = carDetailsService.getById(uuid);
        carDetailsOptional.ifPresent(carDetails -> {
            carDetails.setCarState(ARCHIVED);
            carDetailsService.update(carDetails);
        });
        sendMessages(update, clientInfo);
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {

        final var text = messageSource.getMessage("carDeleted");
        final var callbackQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .text(text)
                .build();
        inomarkaStore.execute(callbackQuery);

        final var id = getIdFromCallbackData(update);
        final var jsonObject = new Gson()
                .fromJson(clientInfo.getAdditionalJsonDataForNextOperations(), JsonObject.class);
        clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
        clientInfoService.update(clientInfo);

        final var messageId = jsonObject.remove(id.toString()).getAsInt();
        final var user = getUserFromUpdate(update);

        final var deleteMessage = DeleteMessage.builder()
                .messageId(messageId)
                .chatId(user.getId())
                .build();
        inomarkaStore.execute(deleteMessage);
    }
}
