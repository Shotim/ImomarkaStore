package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.constant.OwnerState.SAVE_EMAIL;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static com.external.imomarkastore.util.ValidationUtils.formatAndValidateEmail;

@Service
@RequiredArgsConstructor
public class OwnerSaveEmailExecutionService  implements OwnerActionExecuteService {
    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerGetContactsExecutionService ownerGetContactsExecutionService;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return SAVE_EMAIL.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var user = getUserFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        try {
            final var text = getTextFromUpdate(update);
            final var email = formatAndValidateEmail(text);
            ownerInfoService.updateEmail(email);
            final var message = messageSource.getMessage("owner.editEmail.success");
            final var textMessageForUser = createTextMessageForUser(user, message);
            final var successfulEmailSaveMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
            jsonDataObject.add("receivedSaveEmailMessageId", new JsonPrimitive(messageIdFromUpdate));
            jsonDataObject.add("successfulEmailSaveMessageId", new JsonPrimitive(successfulEmailSaveMessageId));
            ownerInfoService.updateJsonData(jsonDataObject.toString());
            ownerInfoService.updateState(GET_CONTACTS);
            deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user, jsonDataObject);
            ownerInfoService.updateJsonData(null);
            ownerGetContactsExecutionService.execute(update);
        } catch (IllegalArgumentException exception) {
            final var errorText = messageSource.getMessage("error.wrongEmailFormat");
            final var errorMessageForUser = createTextMessageForUser(user, errorText);
            final var errorMessageId = inomarkaStore.execute(errorMessageForUser).getMessageId();
            addMessageIdToPropertyOfJsonArray(jsonDataObject, errorMessageId, "emailErrorMessageIds");
            addMessageIdToPropertyOfJsonArray(jsonDataObject, messageIdFromUpdate, "receivedEmailErrorMessageIds");
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
