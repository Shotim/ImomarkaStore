package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;

import static com.external.imomarkastore.constant.OwnerState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.MAIN_MENU;
import static com.external.imomarkastore.util.JsonUtils.extractMessageIds;
import static com.external.imomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerBackToMainMenuExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return BACK_TO_MAIN_MENU.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {

        final var jsonObject = ownerInfoService.getJsonDataObject();
        final var messageIds = new ArrayList<Integer>();
        extractMessageIds(jsonObject, messageIds);
        final var user = getUserFromUpdate(update);
        for (Integer messageId : messageIds) {
            try{
                final var deleteMessage = createDeleteMessageForUser(user, messageId);
                inomarkaStore.execute(deleteMessage);
            }catch (TelegramApiRequestException exception){
                log.error("Tried to delete already deleted message.");
            }
        }
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var deleteReceivedMessageForUser = createDeleteMessageForUser(user, messageIdFromUpdate);
        inomarkaStore.execute(deleteReceivedMessageForUser);
        final var text = messageSource.getMessage("youReturnedBackToMainMenu");
        final var sendMessage = createTextMessageForUser(user, text);
        final var messageId = inomarkaStore.execute(sendMessage).getMessageId();
        ownerInfoService.updateState(MAIN_MENU);
        final var newJsonObject = new JsonObject();
        newJsonObject.add("returnToMainMenuMessageId", new JsonPrimitive(messageId));
        ownerInfoService.updateJsonData(newJsonObject.toString());
    }
}
