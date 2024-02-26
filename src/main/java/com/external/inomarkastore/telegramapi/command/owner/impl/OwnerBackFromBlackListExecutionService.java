package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.OwnerState.BACK_FROM_BLACK_LIST;
import static com.external.inomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUUIDIdFromCallbackDataFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerBackFromBlackListExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return BACK_FROM_BLACK_LIST.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var callbackId = getCallbackIdFromUpdate(update);
        final var clientId = getUUIDIdFromCallbackDataFromUpdate(update);
        final var clientInfo = clientInfoService.getById(clientId);
        clientInfo.setIsInBlackList(false);
        clientInfoService.update(clientInfo);
        final var text = messageSource.getMessage("owner.callback.removeClientFromBlackList");
        final var answerCallbackQuery = createAnswerCallbackQuery(callbackId, text);
        inomarkaStore.execute(answerCallbackQuery);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var clientInfoMessageId = jsonDataObject.remove(clientId.toString()).getAsInt();
        deleteMessagesHelper.deleteMessageById(user.getId(), clientInfoMessageId);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        final var clientInfoTelegramUserId = clientInfo.getTelegramUserId();
        final var youActivatedText = messageSource.getMessage("youActivated");
        final var textMessageForClient = createTextMessageForUser(clientInfoTelegramUserId, youActivatedText);
        inomarkaStore.execute(textMessageForClient);
    }
}
