package com.external.inomarkastore.telegramapi.command.owner.common;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

import static com.external.inomarkastore.telegramapi.command.util.EntitiesSendHelperUtils.getPhotoIds;
import static com.external.inomarkastore.util.MessageUtils.createSendPhotoForUserWithInlineKeyboard;
import static com.external.inomarkastore.util.MessageUtils.createSendPhotoGroupForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButtons;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;

@Component
@RequiredArgsConstructor
public class EntitiesSendHelper {

    private final CarDetailsService carDetailsService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    public void createAndSendApplicationMessage(Long telegramUserId, Application application, JsonArray messageIds, Map<String, String> buttonTextToCallbackData) throws TelegramApiException {
        final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
        final var photoIds = getPhotoIds(application, carDetailsOptional);
        final var text = applicationService.getApplicationPayloadForOwner(application);
        if (photoIds.size() == 1) {
            sendApplicationMessageWithOnePhoto(telegramUserId, messageIds, buttonTextToCallbackData, photoIds, text);
        } else {
            final var applicationMessageId = sendApplicationTextMessage(telegramUserId, messageIds, buttonTextToCallbackData, text);
            if (photoIds.size() > 1) {
                final var sendPhotoGroup = createSendPhotoGroupForUser(telegramUserId, applicationMessageId, photoIds);
                inomarkaStore.execute(sendPhotoGroup)
                        .forEach(mediaMessage -> messageIds.add(new JsonPrimitive(mediaMessage.getMessageId())));
            }
        }
    }

    private Integer sendApplicationTextMessage(Long telegramUserId, JsonArray messageIds, Map<String, String> buttonTextToCallbackData, String text) throws TelegramApiException {
        final var textMessageWithInlineButton = createTextMessageForUserWithInlineButtons(
                telegramUserId, text, buttonTextToCallbackData);
        final var applicationMessage = inomarkaStore.execute(textMessageWithInlineButton);
        final var applicationMessageId = applicationMessage.getMessageId();
        messageIds.add(new JsonPrimitive(applicationMessageId));
        return applicationMessageId;
    }

    private void sendApplicationMessageWithOnePhoto(Long telegramUserId, JsonArray messageIds, Map<String, String> buttonTextToCallbackData, List<String> photoIds, String text) throws TelegramApiException {
        final var sendPhoto = createSendPhotoForUserWithInlineKeyboard(
                telegramUserId, text, photoIds.get(0), buttonTextToCallbackData);
        final var photoMessageId = inomarkaStore.execute(sendPhoto).getMessageId();
        messageIds.add(new JsonPrimitive(photoMessageId));
    }

    public void sendApplicationsMessageForOwner(String code, Long telegramUserId, JsonObject jsonObject) throws TelegramApiException {
        final var message = messageSource.getMessage(code);
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.exportApplications"),
                messageSource.getMessage("buttonName.owner.backToMainMenu")
        );
        final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(telegramUserId, message, buttonNames);
        final var messageId = inomarkaStore.execute(sendMessage).getMessageId();
        jsonObject.add("root", new JsonPrimitive(messageId));
    }

    public void sendMessageForOwnerWithBackToMainMenuButton(String code, Long telegramUserId, JsonObject jsonObject) throws TelegramApiException {
        final var message = messageSource.getMessage(code);
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.backToMainMenu")
        );
        final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(telegramUserId, message, buttonNames);
        final var messageId = inomarkaStore.execute(sendMessage).getMessageId();
        jsonObject.add("root", new JsonPrimitive(messageId));
    }
}
