package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

import static com.external.inomarkastore.constant.OwnerState.EDIT_ADDRESS;
import static com.external.inomarkastore.constant.OwnerState.EDIT_EMAIL;
import static com.external.inomarkastore.constant.OwnerState.EDIT_INN;
import static com.external.inomarkastore.constant.OwnerState.EDIT_NAME;
import static com.external.inomarkastore.constant.OwnerState.EDIT_PHONE_NUMBER;
import static com.external.inomarkastore.constant.OwnerState.EDIT_WORKING_HOURS;
import static com.external.inomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButtons;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerGetContactsExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final BotMessageSource messageSource;
    private final InomarkaStore inomarkaStore;

    @Override
    public String getCommand() {
        return GET_CONTACTS.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var text = ownerInfoService.createContactsPayload();
        final var buttonTextToCallbackData = Map.of(
                messageSource.getMessage("buttonName.owner.editName"), EDIT_NAME.name(),
                messageSource.getMessage("buttonName.owner.editPhoneNumber"), EDIT_PHONE_NUMBER.name(),
                messageSource.getMessage("buttonName.owner.editAddress"), EDIT_ADDRESS.name(),
                messageSource.getMessage("buttonName.owner.editInn"), EDIT_INN.name(),
                messageSource.getMessage("buttonName.owner.editEmail"), EDIT_EMAIL.name(),
                messageSource.getMessage("buttonName.owner.editWorkingHours"), EDIT_WORKING_HOURS.name()
        );
        final var rootMessages = new JsonArray();

        final var contactsPayloadMessage = createTextMessageForUserWithInlineButtons(user.getId(), text, buttonTextToCallbackData);
        final var contactsPayloadMessageId = inomarkaStore.execute(contactsPayloadMessage).getMessageId();
        rootMessages.add(contactsPayloadMessageId);

        final var additionalPayload = messageSource.getMessage("owner.editContactsOrBackToMainMenu");
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.backToMainMenu")
        );
        final var additionalPayloadMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), additionalPayload, buttonNames);
        final var additionalPayloadMessageId = inomarkaStore.execute(additionalPayloadMessage).getMessageId();
        rootMessages.add(additionalPayloadMessageId);

        ownerInfoService.updateState(GET_CONTACTS);
        final var jsonObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonObject.add("receivedGetContactsMessageId", new JsonPrimitive(messageIdFromUpdate));
        jsonObject.add("root", rootMessages);
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
