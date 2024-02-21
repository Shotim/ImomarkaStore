package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

import static com.external.imomarkastore.constant.ApplicationStatus.FULLY_CREATED;
import static com.external.imomarkastore.constant.ClientState.INSERT_COMMENT;
import static com.external.imomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.imomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.imomarkastore.util.MessageUtils.createClientTextMessageWithReplyKeyboardForMainMenu;
import static com.external.imomarkastore.util.MessageUtils.createDeleteMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    @SneakyThrows
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) {
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
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
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
