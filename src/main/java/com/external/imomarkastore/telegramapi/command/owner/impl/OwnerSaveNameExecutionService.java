package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.constant.OwnerState.SAVE_NAME;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerSaveNameExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerGetContactsExecutionService ownerGetContactsExecutionService;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return SAVE_NAME.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var text = getTextFromUpdate(update);
        ownerInfoService.updateName(text);
        final var user = getUserFromUpdate(update);
        final var message = messageSource.getMessage("owner.editName.success");
        final var textMessageForUser = createTextMessageForUser(user.getId(), message);
        final var successfulNameSaveMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonDataObject.add("receivedSaveNameMessageId", new JsonPrimitive(messageIdFromUpdate));
        jsonDataObject.add("successfulNameSaveMessageId", new JsonPrimitive(successfulNameSaveMessageId));
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        ownerInfoService.updateState(GET_CONTACTS);
        deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user.getId(), jsonDataObject);
        ownerInfoService.updateJsonData(null);
        ownerGetContactsExecutionService.execute(update);
    }
}
