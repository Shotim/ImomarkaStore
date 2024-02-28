package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.OwnerState.GET_PHOTO;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerGetPhotoExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return GET_PHOTO.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var user = getUserFromUpdate(update);
        final var text = getTextFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var sendPhoto = SendPhoto.builder()
                .photo(new InputFile(text))
                .chatId(user.getId())
                .build();
        addMessageIdToPropertyOfJsonArray(jsonDataObject, messageIdFromUpdate, "receivedGetPhotoMessageIds");
        try {
            final var messageWithPhotoId = inomarkaStore.execute(sendPhoto).getMessageId();
            addMessageIdToPropertyOfJsonArray(jsonDataObject, messageWithPhotoId, "getPhotoMessageIds");
        } catch (TelegramApiException e) {
            final var errorText = messageSource.getMessage("error.wrongPhotoId");
            final var errorMessage = createTextMessageForUser(user.getId(), errorText);
            final var errorMessageId = inomarkaStore.execute(errorMessage).getMessageId();
            addMessageIdToPropertyOfJsonArray(jsonDataObject, errorMessageId, "getPhotoMessageIds");
        }
        ownerInfoService.updateJsonData(jsonDataObject.toString());
    }

    private void addMessageIdToPropertyOfJsonArray(JsonObject jsonDataObject, Integer messageId, String property) {
        if (jsonDataObject.has(property)) {
            jsonDataObject.get(property).getAsJsonArray().add(new JsonPrimitive(messageId));
        } else {
            final var messageIds = new JsonArray();
            messageIds.add(messageId);
            jsonDataObject.add(property, messageIds);
        }
    }
}