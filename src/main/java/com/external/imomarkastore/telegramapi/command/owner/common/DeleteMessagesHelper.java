package com.external.imomarkastore.telegramapi.command.owner.common;

import com.external.imomarkastore.InomarkaStore;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;

import static com.external.imomarkastore.util.MessageUtils.createDeleteMessageForUser;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMessagesHelper {

    private final InomarkaStore inomarkaStore;

    public void deleteAllMessagesFromJsonDataForUser(Long telegramUserId, JsonObject jsonDataObject) throws TelegramApiException {
        final var messageIds = new ArrayList<Integer>();
        extractMessageIds(jsonDataObject, messageIds);
        for (Integer messageId : messageIds) {
            deleteMessageById(telegramUserId, messageId);
        }
    }

    public void deleteMessageById(Long telegramUserId, Integer messageId) throws TelegramApiException {
        try {
            final var deleteMessage = createDeleteMessageForUser(telegramUserId, messageId);
            inomarkaStore.execute(deleteMessage);
        } catch (TelegramApiRequestException exception) {
            log.error("Tried to delete already deleted message.");
        }
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
