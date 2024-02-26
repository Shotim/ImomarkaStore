package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.OwnerState.EDIT_PHONE_NUMBER;
import static com.external.inomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerEditPhoneNumberExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return EDIT_PHONE_NUMBER.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
        final var text = messageSource.getMessage("owner.callback.editPhoneNumber");
        final var answerCallbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, text);
        inomarkaStore.execute(answerCallbackQuery);

        final var user = getUserFromUpdate(update);
        final var message = messageSource.getMessage("owner.editPhoneNumber");
        final var textMessageForUser = createTextMessageForUser(user.getId(), message);
        final var editPhoneNumberMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        jsonDataObject.add("editPhoneNumberMessageId", new JsonPrimitive(editPhoneNumberMessageId));
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        ownerInfoService.updateState(EDIT_PHONE_NUMBER);
    }
}
