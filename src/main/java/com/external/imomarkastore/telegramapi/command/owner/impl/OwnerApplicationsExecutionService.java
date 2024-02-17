package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.util.Optional;
import java.util.stream.Stream;

import static com.external.imomarkastore.constant.OwnerState.GET_APPLICATIONS;
import static com.external.imomarkastore.constant.OwnerState.MOVE_APPLICATION_TO_ARCHIVE;
import static com.external.imomarkastore.util.MessageUtils.createInlineKeyboardMarkup;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithButtonBackToMainMenuForOwner;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithInlineButton;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerApplicationsExecutionService implements OwnerActionExecuteService {

    private final ApplicationService applicationService;
    private final OwnerInfoService ownerInfoService;
    private final CarDetailsService carDetailsService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return GET_APPLICATIONS.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var applications = applicationService.getFullyCreatedApplications();
        final var jsonObject = new JsonObject();
        if (applications.isEmpty()) {
            final var message = messageSource.getMessage("owner.youDoNotHaveActiveApplications");
            final var sendMessage = createTextMessageWithButtonBackToMainMenuForOwner(user, message);
            final var messageId = inomarkaStore.execute(sendMessage).getMessageId();
            jsonObject.add("root", new JsonPrimitive(messageId));
        } else {
            final var message = messageSource.getMessage("owner.yourActiveApplications");
            final var sendMessage = createTextMessageWithButtonBackToMainMenuForOwner(user, message);
            final var messageId = inomarkaStore.execute(sendMessage).getMessageId();
            jsonObject.add("root", new JsonPrimitive(messageId));
            for (Application application : applications) {
                final var messageIds = new JsonArray();
                jsonObject.add(application.getId().toString(), messageIds);
                final var text = applicationService.getApplicationPayloadForOwner(application);
                final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
                final var photoIds = Stream.of(
                                Optional.ofNullable(application.getMainPurposePhotoId()),
                                carDetailsOptional.map(CarDetails::getVinNumberPhotoId))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

                final var archiveApplicationButtonName =
                        messageSource.getMessage("buttonName.owner.archiveApplication");
                final var callbackData = "%s:%s".formatted(MOVE_APPLICATION_TO_ARCHIVE, application.getId());
                final var inlineKeyboardMarkup = createInlineKeyboardMarkup(
                        archiveApplicationButtonName, callbackData);
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
        }
        ownerInfoService.updateState(GET_APPLICATIONS);
        ownerInfoService.updateJsonData(jsonObject.toString());
    }
}
