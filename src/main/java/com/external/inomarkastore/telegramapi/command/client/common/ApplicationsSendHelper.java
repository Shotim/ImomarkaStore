package com.external.inomarkastore.telegramapi.command.client.common;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.model.Application;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.telegramapi.command.util.EntitiesSendHelperUtils.getPhotoIds;
import static com.external.inomarkastore.util.MessageUtils.createSendPhotoForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;

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
                final var carDetails = carDetailsService.getById(application.getCarDetailsId());
                final var photoIds = getPhotoIds(application, carDetails);
                if (photoIds.size() == 1) {
                    final var sendPhotoForUser = createSendPhotoForUser(
                            telegramUserId, applicationPayload, photoIds.get(0));
                    inomarkaStore.execute(sendPhotoForUser);
                } else {
                    final var applicationText = createTextMessageForUser(telegramUserId, applicationPayload);
                    final var message = inomarkaStore.execute(applicationText);
                    if (photoIds.size() > 1) {
                        final var sendMediaGroup = MessageUtils.createSendPhotoGroupForUserPhotoIds(
                                telegramUserId, message.getMessageId(), photoIds);
                        inomarkaStore.execute(sendMediaGroup);
                    }
                }
            }
        }
    }
}
