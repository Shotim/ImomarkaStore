package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackDataFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    @SneakyThrows
    public void execute(Update update) {
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
