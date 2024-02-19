package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;

import static com.external.imomarkastore.constant.OwnerState.GET_CONTACTS;
import static com.external.imomarkastore.constant.OwnerState.SAVE_NAME;
import static com.external.imomarkastore.util.JsonUtils.extractMessageIds;
import static com.external.imomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerSaveNameExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerGetContactsExecutionService ownerGetContactsExecutionService;

    @Override
    public String getCommand() {
        return SAVE_NAME.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var text = getTextFromUpdate(update);
        ownerInfoService.updateName(text);
        final var user = getUserFromUpdate(update);
        final var message = messageSource.getMessage("owner.editName.success");
        final var textMessageForUser = createTextMessageForUser(user, message);
        final var successfulNameSaveMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        jsonDataObject.add("receivedSaveNameMessageId", new JsonPrimitive(messageIdFromUpdate));
        jsonDataObject.add("successfulNameSaveMessageId", new JsonPrimitive(successfulNameSaveMessageId));
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        ownerInfoService.updateState(GET_CONTACTS);
        final var messageIds = new ArrayList<Integer>();
        extractMessageIds(jsonDataObject, messageIds);
        for (Integer messageId : messageIds) {
            try{
                final var deleteMessage = createDeleteMessageForUser(user, messageId);
                inomarkaStore.execute(deleteMessage);
            }catch (TelegramApiRequestException exception){
                log.error("Tried to delete already deleted message.");
            }
        }
        ownerInfoService.updateJsonData(null);
        ownerGetContactsExecutionService.execute(update);
    }
}
