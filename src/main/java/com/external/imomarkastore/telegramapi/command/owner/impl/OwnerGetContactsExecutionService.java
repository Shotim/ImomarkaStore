package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

import static com.external.imomarkastore.constant.OwnerState.EDIT_ADDRESS;
import static com.external.imomarkastore.constant.OwnerState.EDIT_EMAIL;
import static com.external.imomarkastore.constant.OwnerState.EDIT_INN;
import static com.external.imomarkastore.constant.OwnerState.EDIT_NAME;
import static com.external.imomarkastore.constant.OwnerState.EDIT_PHONE_NUMBER;
import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButtons;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var text = ownerInfoService.createContactsPayload();
        final var buttonTextToCallbackData = Map.of(
                messageSource.getMessage("buttonName.owner.editName"), EDIT_NAME.name(),
                messageSource.getMessage("buttonName.owner.editPhoneNumber"), EDIT_PHONE_NUMBER.name(),
                messageSource.getMessage("buttonName.owner.editAddress"), EDIT_ADDRESS.name(),
                messageSource.getMessage("buttonName.owner.editInn"), EDIT_INN.name(),
                messageSource.getMessage("buttonName.owner.editEmail"), EDIT_EMAIL.name()
        );
        final var rootMessages = new JsonArray();

        final var contactsPayloadMessage = createTextMessageForUserWithInlineButtons(user, text, buttonTextToCallbackData);
        final var contactsPayloadMessageId = inomarkaStore.execute(contactsPayloadMessage).getMessageId();
        rootMessages.add(contactsPayloadMessageId);

        final var additionalPayload = messageSource.getMessage("owner.editContactsOrBackToMainMenu");
        final var additionalPayloadMessage = createTextMessageForUser(user, additionalPayload);
        final var additionalPayloadMessageId = inomarkaStore.execute(additionalPayloadMessage).getMessageId();
        rootMessages.add(additionalPayloadMessageId);

        ownerInfoService.updateState(GET_CONTACTS);
        final var jsonObject = new JsonObject();
        jsonObject.add("root", rootMessages);
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
