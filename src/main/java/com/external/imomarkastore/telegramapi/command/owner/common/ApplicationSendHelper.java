package com.external.imomarkastore.telegramapi.command.owner.common;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.external.imomarkastore.util.MessageUtils.createInlineKeyBoardMarkup;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithButtonBackToMainMenuForOwner;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithInlineButton;

@Component
@RequiredArgsConstructor
public class ApplicationSendHelper {

    private final CarDetailsService carDetailsService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    public void createAndSendApplicationMessage(User user, Application application, JsonArray messageIds, String archiveApplicationButtonName, String callbackData) throws TelegramApiException {
        final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
        final var photoIds = Stream.of(
                        Optional.ofNullable(application.getMainPurposePhotoId()),
                        carDetailsOptional.map(CarDetails::getVinNumberPhotoId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        final var text = applicationService.getApplicationPayloadForOwner(application);
        final var inlineKeyboardMarkup = createInlineKeyBoardMarkup(
                Map.of(archiveApplicationButtonName, callbackData));
        if (photoIds.size() == 1) {
            final var sendPhoto = SendPhoto.builder()
                    .caption(text)
                    .chatId(user.getId().toString())
                    .photo(new InputFile(photoIds.get(0)))
                    .replyMarkup(inlineKeyboardMarkup)
                    .build();
            final var photoMessageId = inomarkaStore.execute(sendPhoto).getMessageId();
            messageIds.add(new JsonPrimitive(photoMessageId));
        } else {
            final var textMessageWithInlineButton = createTextMessageWithInlineButton(
                    user, text, archiveApplicationButtonName, callbackData);
            final var applicationMessage = inomarkaStore.execute(textMessageWithInlineButton);
            messageIds.add(new JsonPrimitive(applicationMessage.getMessageId()));
            if (photoIds.size() > 1) {
                final var inputMediaPhotos = photoIds.stream()
                        .map(photoId ->
                                (InputMedia) new InputMediaPhoto(photoId))
                        .toList();
                final var sendMediaGroup = SendMediaGroup.builder()
                        .chatId(user.getId().toString())
                        .messageThreadId(applicationMessage.getMessageThreadId())
                        .replyToMessageId(applicationMessage.getMessageId())
                        .medias(inputMediaPhotos)
                        .build();
                inomarkaStore.execute(sendMediaGroup).forEach(mediaMessage -> messageIds.add(new JsonPrimitive(mediaMessage.getMessageId())));
            }
        }
    }

    public void sendApplicationsMessageForOwner(String code, User user, JsonObject jsonObject) throws TelegramApiException {
        final var message = messageSource.getMessage(code);
        final var sendMessage = createTextMessageWithButtonBackToMainMenuForOwner(user, message);
        final var messageId = inomarkaStore.execute(sendMessage).getMessageId();
        jsonObject.add("root", new JsonPrimitive(messageId));
    }
}
