package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;

import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.constant.OwnerState.SAVE_PHONE_NUMBER;
import static com.external.imomarkastore.util.JsonUtils.extractMessageIds;
import static com.external.imomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static com.external.imomarkastore.util.ValidationUtils.formatAndValidatePhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerSavePhoneNumberExecutionService implements OwnerActionExecuteService {
    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerGetContactsExecutionService ownerGetContactsExecutionService;

    @Override
    public String getCommand() {
        return SAVE_PHONE_NUMBER.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var user = getUserFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        try {
            final var text = getTextFromUpdate(update);
            final var phoneNumber = formatAndValidatePhoneNumber(text);
            ownerInfoService.updatePhoneNumber(phoneNumber);
            final var message = messageSource.getMessage("owner.editPhoneNumber.success");
            final var textMessageForUser = createTextMessageForUser(user, message);
            final var successfulPhoneNumberSaveMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
            jsonDataObject.add("receivedSavePhoneNumberMessageId", new JsonPrimitive(messageIdFromUpdate));
            jsonDataObject.add("successfulPhoneNumberSaveMessageId", new JsonPrimitive(successfulPhoneNumberSaveMessageId));
            ownerInfoService.updateJsonData(jsonDataObject.toString());
            ownerInfoService.updateState(GET_CONTACTS);
            final var messageIds = new ArrayList<Integer>();
            extractMessageIds(jsonDataObject, messageIds);
            for (Integer messageId : messageIds) {
                deleteMessage(user, messageId);
            }
            ownerInfoService.updateJsonData(null);
            ownerGetContactsExecutionService.execute(update);
        } catch (IllegalArgumentException exception) {
            final var errorText = messageSource.getMessage("error.wrongPhoneNumberFormat");
            final var errorMessageForUser = createTextMessageForUser(user, errorText);
            final var errorMessageId = inomarkaStore.execute(errorMessageForUser).getMessageId();
            addMessageIdToPropertyOfJsonArray(jsonDataObject, errorMessageId, "phoneNumberErrorMessageIds");
            addMessageIdToPropertyOfJsonArray(jsonDataObject, messageIdFromUpdate, "receivedPhoneNumberErrorMessageIds");
            ownerInfoService.updateJsonData(jsonDataObject.toString());
        }
    }

    private void addMessageIdToPropertyOfJsonArray(JsonObject jsonDataObject, Integer errorMessageId, String property) {
        if (jsonDataObject.has(property)) {
            jsonDataObject.get(property).getAsJsonArray().add(new JsonPrimitive(errorMessageId));
        } else {
            final var errorMessageIds = new JsonArray();
            errorMessageIds.add(errorMessageId);
            jsonDataObject.add(property, errorMessageIds);
        }
    }

    private void deleteMessage(User user, Integer messageId) throws TelegramApiException {
        try {
            final var deleteMessage = createDeleteMessageForUser(user, messageId);
            inomarkaStore.execute(deleteMessage);
        } catch (TelegramApiRequestException exception) {
            log.error("Tried to delete already deleted message.");
        }
    }
}
