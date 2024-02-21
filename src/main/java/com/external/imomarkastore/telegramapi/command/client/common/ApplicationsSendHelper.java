package com.external.imomarkastore.telegramapi.command.client.common;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.model.Application;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.telegramapi.command.util.EntitiesSendHelperUtils.getPhotoIds;
import static com.external.imomarkastore.util.MessageUtils.createSendPhotoForUser;
import static com.external.imomarkastore.util.MessageUtils.createSendPhotoGroupForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;

@Component
@RequiredArgsConstructor
public class ApplicationsSendHelper {

    private final InomarkaStore inomarkaStore;
    private final ApplicationService applicationService;
    private final CarDetailsService carDetailsService;

    public void sendApplications(Long telegramUserId, List<Application> applications, String text)
            throws TelegramApiException {
        final var outputMessage = createTextMessageForUserWithRemoveKeyBoard(telegramUserId, text);
        inomarkaStore.execute(outputMessage);

        if (!applications.isEmpty()) {
            for (Application application : applications) {
                final var applicationPayload = applicationService.getApplicationPayloadForClient(application);
                final var carDetailsOptional = carDetailsService.getById(application.getCarDetailsId());
                final var photoIds = getPhotoIds(application, carDetailsOptional);
                if (photoIds.size() == 1) {
                    final var sendPhotoForUser = createSendPhotoForUser(
                            telegramUserId, applicationPayload, photoIds.get(0));
                    inomarkaStore.execute(sendPhotoForUser);
                } else {
                    final var applicationText = createTextMessageForUser(telegramUserId, applicationPayload);
                    final var message = inomarkaStore.execute(applicationText);
                    if (photoIds.size() > 1) {
                        final var sendMediaGroup = createSendPhotoGroupForUser(
                                telegramUserId, message.getMessageId(), photoIds);
                        inomarkaStore.execute(sendMediaGroup);
                    }
                }
            }
        }
    }
}
