package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.MAIN_MENU;
import static com.external.imomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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

        final var jsonData = ownerInfoService.getJsonData();
        final var jsonObject = new Gson().fromJson(jsonData, JsonObject.class);
        final var messageIds = new ArrayList<Integer>();
        extractMessageIds(jsonObject, messageIds);
        final var user = getUserFromUpdate(update);
        for (Integer messageId : messageIds) {
            final var deleteMessage = createDeleteMessageForUser(user, messageId);
            inomarkaStore.execute(deleteMessage);
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

    private void extractMessageIds(JsonElement jsonElement, List<Integer> messageIds) {
        if (jsonElement.isJsonObject()) {
            final var jsonObject = jsonElement.getAsJsonObject();
            jsonObject.keySet()
                    .forEach(key -> extractMessageIds(jsonObject.get(key), messageIds));
        } else if (jsonElement.isJsonArray()) {
            jsonElement.getAsJsonArray()
                    .forEach(jsonArrayEntry -> extractMessageIds(jsonArrayEntry, messageIds));
        } else if (jsonElement.isJsonPrimitive()) {
            messageIds.add(jsonElement.getAsInt());
        }
    }
}
