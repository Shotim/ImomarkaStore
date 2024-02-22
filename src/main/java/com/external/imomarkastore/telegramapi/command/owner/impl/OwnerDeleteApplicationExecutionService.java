package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.imomarkastore.constant.OwnerState.DELETE_APPLICATION;
import static com.external.imomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getLongIdFromCallbackDataFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerDeleteApplicationExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return DELETE_APPLICATION.name();
    }

    @Override
    @Transactional
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var applicationId = getLongIdFromCallbackDataFromUpdate(update);
        final var callbackId = getCallbackIdFromUpdate(update);
        final var applicationOptional = applicationService.getById(applicationId);
        if (applicationOptional.isPresent()) {
            applicationService.deleteById(applicationId);
            final var text = messageSource.getMessage("owner.callback.deleteApplication");
            final var answerCallbackQuery = createAnswerCallbackQuery(callbackId, text);
            inomarkaStore.execute(answerCallbackQuery);
            final var jsonDataObject = ownerInfoService.getJsonDataObject();
            final var messageIdsToDelete = jsonDataObject.remove(applicationId.toString()).getAsJsonArray();
            for (JsonElement jsonElement : messageIdsToDelete) {
                final var messageId = jsonElement.getAsInt();
                deleteMessagesHelper.deleteMessageById(user.getId(), messageId);
            }
            ownerInfoService.updateJsonData(jsonDataObject.toString());
        }
    }
}
