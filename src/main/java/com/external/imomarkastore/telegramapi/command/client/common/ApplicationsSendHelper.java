package com.external.imomarkastore.telegramapi.command.client.common;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;

@Component
@RequiredArgsConstructor
public class ApplicationsSendHelper {

    private final InomarkaStore inomarkaStore;
    private final ApplicationService applicationService;
    private final CarDetailsService carDetailsService;

    public void sendApplications(User user, List<Application> applications, String text) throws TelegramApiException {
        final var outputMessage = createTextMessageForUserWithRemoveKeyBoard(user, text);
        inomarkaStore.execute(outputMessage);

        if (!applications.isEmpty()) {
            for (Application application : applications) {
                final var applicationPayload = applicationService.getApplicationPayloadForClient(application);
                final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
                final var photoIds = Stream.of(
                                Optional.ofNullable(application.getMainPurposePhotoId()),
                                carDetailsOptional.map(CarDetails::getVinNumberPhotoId))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
                if (photoIds.size() == 1) {
                    final var sendPhoto = SendPhoto.builder()
                            .caption(applicationPayload)
                            .chatId(user.getId().toString())
                            .photo(new InputFile(photoIds.get(0)))
                            .build();
                    inomarkaStore.execute(sendPhoto);
                } else {
                    final var applicationText = createTextMessageForUser(user, applicationPayload);
                    final var message = inomarkaStore.execute(applicationText);
                    if (photoIds.size() > 1) {
                        final var inputMediaPhotos = photoIds.stream()
                                .map(photoId ->
                                        (InputMedia) new InputMediaPhoto(photoId))
                                .toList();
                        final var sendMediaGroup = SendMediaGroup.builder()
                                .chatId(user.getId().toString())
                                .messageThreadId(message.getMessageThreadId())
                                .replyToMessageId(message.getMessageId())
                                .medias(inputMediaPhotos)
                                .build();
                        inomarkaStore.execute(sendMediaGroup);
                    }
                }
            }
        }
    }
}
