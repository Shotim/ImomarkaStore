package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.OwnerState.EDIT_INN;
import static com.external.imomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerEditInnExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return EDIT_INN.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
        final var text = messageSource.getMessage("owner.callback.editInn");
        final var answerCallbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, text);
        inomarkaStore.execute(answerCallbackQuery);

        final var user = getUserFromUpdate(update);
        final var message = messageSource.getMessage("owner.editInn");
        final var textMessageForUser = createTextMessageForUser(user.getId(), message);
        final var editInnMessageId = inomarkaStore.execute(textMessageForUser).getMessageId();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        jsonDataObject.add("editInnMessageId", new JsonPrimitive(editInnMessageId));
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        ownerInfoService.updateState(EDIT_INN);
    }
}
