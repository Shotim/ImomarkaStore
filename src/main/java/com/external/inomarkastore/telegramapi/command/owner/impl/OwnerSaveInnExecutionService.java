package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.inomarkastore.constant.OwnerState.SAVE_INN;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerSaveInnExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerGetContactsExecutionService ownerGetContactsExecutionService;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return SAVE_INN.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var text = getTextFromUpdate(update);
        ownerInfoService.updateInn(text);
        final var user = getUserFromUpdate(update);
        final var message = messageSource.getMessage("owner.editInn.success");
        final var textMessageForUser = createTextMessageForUser(user.getId(), message);
        final var successfulInnSaveMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonDataObject.add("receivedSaveInnMessageId", new JsonPrimitive(messageIdFromUpdate));
        jsonDataObject.add("successfulInnSaveMessageId", new JsonPrimitive(successfulInnSaveMessageId));
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        ownerInfoService.updateState(GET_CONTACTS);
        deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user.getId(), jsonDataObject);
        ownerInfoService.updateJsonData(null);
        ownerGetContactsExecutionService.execute(update);
    }
}
