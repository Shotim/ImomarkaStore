package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.OwnerState.BACK_FROM_BLACK_LIST;
import static com.external.imomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUUIDIdFromCallbackData;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var callbackId = getCallbackIdFromUpdate(update);
        final var clientId = getUUIDIdFromCallbackData(update);
        final var clientInfoOptional = clientInfoService.getById(clientId);
        if (clientInfoOptional.isPresent()) {
            final var clientInfo = clientInfoOptional.get();
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
}
