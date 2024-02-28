package com.external.inomarkastore.telegramapi.command.owner.common;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.util.BotMessageSource;
import com.external.inomarkastore.util.MessageUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.external.inomarkastore.telegramapi.command.util.EntitiesSendHelperUtils.getPhotoIds;
import static com.external.inomarkastore.util.MessageUtils.createSendPhotoForUserWithInlineKeyboard;
import static com.external.inomarkastore.util.MessageUtils.createSendPhotoGroupForUserFiles;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButtons;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static java.util.Collections.emptyList;

@Component
@RequiredArgsConstructor
public class EntitiesSendHelper {

    private final CarDetailsService carDetailsService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    public void createAndSendApplicationMessage(Long telegramUserId, Application application, JsonArray messageIds, Map<String, String> buttonTextToCallbackData) throws TelegramApiException {
        final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
        final var photos = getPhotoIds(application, carDetailsOptional);
        final var text = applicationService.getApplicationPayloadForOwner(application);
        if (photos.size() == 1) {
            sendApplicationMessageWithOnePhoto(telegramUserId, messageIds, buttonTextToCallbackData, photos.get(0), text);
        } else {
            final var applicationMessageId = sendApplicationTextMessage(telegramUserId, messageIds, buttonTextToCallbackData, text);
            if (photos.size() > 1) {
                final var sendPhotoGroup = MessageUtils.createSendPhotoGroupForUserPhotoIds(telegramUserId, applicationMessageId, photos);
                inomarkaStore.execute(sendPhotoGroup)
                        .forEach(mediaMessage -> messageIds.add(new JsonPrimitive(mediaMessage.getMessageId())));
            }
        }
    }

    public List<Message> createAndSendApplicationMessage(Long telegramUserId, Application application, JsonArray messageIds, Map<String, String> buttonTextToCallbackData, List<InputStream> photos) throws TelegramApiException {
        final var text = applicationService.getApplicationPayloadForOwner(application);
        if (photos.size() == 1) {
            return sendApplicationMessageWithOnePhoto(telegramUserId, messageIds, buttonTextToCallbackData, photos.get(0), text);
        } else {
            final var applicationMessageId = sendApplicationTextMessage(telegramUserId, messageIds, buttonTextToCallbackData, text);
            if (photos.size() > 1) {
                final var sendPhotoGroup = createSendPhotoGroupForUserFiles(telegramUserId, applicationMessageId, photos);
                final var messages = inomarkaStore.execute(sendPhotoGroup);
                messages
                        .forEach(mediaMessage -> messageIds.add(new JsonPrimitive(mediaMessage.getMessageId())));
                return messages;
            } else {
                return emptyList();
            }
        }
    }

    private Integer sendApplicationTextMessage(Long telegramUserId, JsonArray messageIds, Map<String, String> buttonTextToCallbackData, String text) throws TelegramApiException {
        final var applicationMessageId = sendApplicationTextMessage(telegramUserId, buttonTextToCallbackData, text);
        messageIds.add(new JsonPrimitive(applicationMessageId));
        return applicationMessageId;
    }

    private Integer sendApplicationTextMessage(Long telegramUserId, Map<String, String> buttonTextToCallbackData, String text) throws TelegramApiException {
        final var textMessageWithInlineButton = createTextMessageForUserWithInlineButtons(
                telegramUserId, text, buttonTextToCallbackData);
        final var applicationMessage = inomarkaStore.execute(textMessageWithInlineButton);
        return applicationMessage.getMessageId();
    }

    private void sendApplicationMessageWithOnePhoto(Long telegramUserId, JsonArray messageIds, Map<String, String> buttonTextToCallbackData, String photoId, String text) throws TelegramApiException {
        final var photoMessageId = sendApplicationMessageWithOnePhoto(telegramUserId, buttonTextToCallbackData, photoId, text);
        messageIds.add(new JsonPrimitive(photoMessageId));
    }

    private List<Message> sendApplicationMessageWithOnePhoto(Long telegramUserId, JsonArray messageIds, Map<String, String> buttonTextToCallbackData, InputStream photo, String text) throws TelegramApiException {
        final var photoMessage = sendApplicationMessageWithOnePhoto(telegramUserId, buttonTextToCallbackData, photo, text);
        messageIds.add(new JsonPrimitive(photoMessage.getMessageId()));
        return List.of(photoMessage);
    }

    private Integer sendApplicationMessageWithOnePhoto(Long telegramUserId, Map<String, String> buttonTextToCallbackData, String photoId, String text) throws TelegramApiException {
        final var sendPhoto = createSendPhotoForUserWithInlineKeyboard(
                telegramUserId, text, photoId, buttonTextToCallbackData);
        return inomarkaStore.execute(sendPhoto).getMessageId();
    }

    private Message sendApplicationMessageWithOnePhoto(Long telegramUserId, Map<String, String> buttonTextToCallbackData, InputStream photo, String text) throws TelegramApiException {
        final var sendPhoto = createSendPhotoForUserWithInlineKeyboard(
                telegramUserId, text, photo, buttonTextToCallbackData);
        return inomarkaStore.execute(sendPhoto);
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
