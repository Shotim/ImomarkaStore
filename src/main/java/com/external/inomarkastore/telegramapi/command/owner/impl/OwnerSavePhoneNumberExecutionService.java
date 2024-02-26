package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.inomarkastore.constant.OwnerState.SAVE_PHONE_NUMBER;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static com.external.inomarkastore.util.ValidationUtils.formatAndValidatePhoneNumber;

@Service
@RequiredArgsConstructor
public class OwnerSavePhoneNumberExecutionService implements OwnerActionExecuteService {
    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerGetContactsExecutionService ownerGetContactsExecutionService;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return SAVE_PHONE_NUMBER.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var user = getUserFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        try {
            final var text = getTextFromUpdate(update);
            final var phoneNumber = formatAndValidatePhoneNumber(text);
            ownerInfoService.updatePhoneNumber(phoneNumber);
            final var message = messageSource.getMessage("owner.editPhoneNumber.success");
            final var textMessageForUser = createTextMessageForUser(user.getId(), message);
            final var successfulPhoneNumberSaveMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
            jsonDataObject.add("receivedSavePhoneNumberMessageId", new JsonPrimitive(messageIdFromUpdate));
            jsonDataObject.add("successfulPhoneNumberSaveMessageId", new JsonPrimitive(successfulPhoneNumberSaveMessageId));
            ownerInfoService.updateJsonData(jsonDataObject.toString());
            ownerInfoService.updateState(GET_CONTACTS);
            deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user.getId(), jsonDataObject);
            ownerInfoService.updateJsonData(null);
            ownerGetContactsExecutionService.execute(update);
        } catch (IllegalArgumentException exception) {
            final var errorText = messageSource.getMessage("error.wrongPhoneNumberFormat");
            final var errorMessageForUser = createTextMessageForUser(user.getId(), errorText);
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
}
