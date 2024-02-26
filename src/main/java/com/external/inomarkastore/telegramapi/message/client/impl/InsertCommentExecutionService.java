package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static com.external.inomarkastore.constant.ApplicationStatus.FULLY_CREATED;
import static com.external.inomarkastore.constant.ClientState.INSERT_COMMENT;
import static com.external.inomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.inomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.inomarkastore.util.MessageUtils.createClientTextMessageWithReplyKeyboardForMainMenu;
import static com.external.inomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class InsertCommentExecutionService implements MessageExecutionService {
    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final OwnerInfoService ownerInfoService;
    private final EntitiesSendHelper entitiesSendHelper;

    @Override
    public ClientState getState() {
        return INSERT_COMMENT;
    }

    @Override
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        clientInfo.setState(MAIN_MENU);
        clientInfoService.update(clientInfo);
        final var application = applicationService.getFirstInProgressByTelegramUserId(clientInfo.getTelegramUserId());
        final var text = update.hasCallbackQuery() ?
                messageSource.getMessage("noComment") :
                getTextFromUpdate(update);
        application.setComment(text);
        application.setStatus(FULLY_CREATED);
        final var ownerUserId = ownerInfoService.getTelegramUserId();
        final var newApplicationCreatedText = messageSource.getMessage("owner.createdNewApplication");
        final var textMessageForOwner = createTextMessageForUser(ownerUserId, newApplicationCreatedText);
        final var sendMessageForOwnerMessageId = inomarkaStore.execute(textMessageForOwner).getMessageId();
        final var messageIds = new JsonArray();
        messageIds.add(sendMessageForOwnerMessageId);
        final var callbackData = "NEW_APPLICATION:%s".formatted(application.getId());
        final var skipApplicationButtonName = messageSource.getMessage("buttonName.owner.skipApplication");
        final var buttonNameToCallbackData = Map.of(
                skipApplicationButtonName,
                callbackData
        );
        final var updatedApplication = applicationService.update(application);
        entitiesSendHelper.createAndSendApplicationMessage(
                ownerUserId, updatedApplication, messageIds, buttonNameToCallbackData);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        jsonDataObject.add(callbackData, messageIds);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        if (update.hasCallbackQuery()) {
            final var text = messageSource.getMessage("commentWasNotAdded");
            final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
            final var callbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, text);
            inomarkaStore.execute(callbackQuery);
        }
        final var jsonDataString = clientInfo.getAdditionalJsonDataForNextOperations();
        final var jsonObject = new Gson().fromJson(jsonDataString, JsonObject.class);
        final var messageId = jsonObject.get("messageId").getAsInt();
        final var deleteMessage = createDeleteMessageForUser(user.getId(), messageId);
        inomarkaStore.execute(deleteMessage);
        final var text = messageSource.getMessage("applicationCreated");
        final var message = createClientTextMessageWithReplyKeyboardForMainMenu(user.getId(), text);
        inomarkaStore.execute(message);

        clientInfo.setAdditionalJsonDataForNextOperations(null);
        clientInfoService.update(clientInfo);
    }
}
