package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.BACK_TO_MAIN_MENU;
import static com.external.imomarkastore.constant.OwnerState.MAIN_MENU;
import static com.external.imomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerBackToMainMenuExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final DeleteMessagesHelper deleteMessagesHelper;

    @Override
    public String getCommand() {
        return BACK_TO_MAIN_MENU.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {

        final var jsonObject = ownerInfoService.getJsonDataObject();
        final var user = getUserFromUpdate(update);
        deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user.getId(), jsonObject);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var deleteReceivedMessageForUser = createDeleteMessageForUser(user.getId(), messageIdFromUpdate);
        inomarkaStore.execute(deleteReceivedMessageForUser);
        final var text = messageSource.getMessage("youReturnedBackToMainMenu");
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.getApplications"),
                messageSource.getMessage("buttonName.owner.getArchivedApplications"),
                messageSource.getMessage("buttonName.owner.getClients"),
                messageSource.getMessage("buttonName.owner.getBlackList"),
                messageSource.getMessage("buttonName.owner.getContacts"),
                messageSource.getMessage("buttonName.owner.getPhoto")
        );
        final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), text, buttonNames);
        final var messageId = inomarkaStore.execute(sendMessage).getMessageId();
        ownerInfoService.updateState(MAIN_MENU);
        final var newJsonObject = new JsonObject();
        newJsonObject.add("returnToMainMenuMessageId", new JsonPrimitive(messageId));
        ownerInfoService.updateJsonData(newJsonObject.toString());
    }
}
