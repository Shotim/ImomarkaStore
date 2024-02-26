package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackDataFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerNewApplicationExecutionService implements OwnerActionExecuteService {
    private final OwnerInfoService ownerInfoService;
    private final DeleteMessagesHelper deleteMessagesHelper;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return "NEW_APPLICATION";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var callbackData = getCallbackDataFromUpdate(update);
        final var user = getUserFromUpdate(update);
        final var callbackId = getCallbackIdFromUpdate(update);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        if (jsonDataObject.has(callbackData)) {
            for (JsonElement messageId : jsonDataObject.remove(callbackId).getAsJsonArray()) {
                final var messageIdAsInt = messageId.getAsInt();
                deleteMessagesHelper.deleteMessageById(user.getId(), messageIdAsInt);
            }
        }
        final var message = messageSource.getMessage("owner.skipApplication");
        final var answerCallbackQuery = createAnswerCallbackQuery(callbackId, message);
        inomarkaStore.execute(answerCallbackQuery);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
    }
}
