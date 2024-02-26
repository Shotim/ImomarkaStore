package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.external.inomarkastore.constant.ClientState.INSERT_VIN_NUMBER;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.inomarkastore.util.UpdateUtils.getPhotoFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static com.external.inomarkastore.util.ValidationUtils.formatAndValidateVinNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertVinNumberExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final CarDetailsService carDetailsService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return INSERT_VIN_NUMBER;
    }

    @Override
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        try {
            final var application = applicationService.getFirstInProgressByTelegramUserId(clientInfo.getTelegramUserId());
            final var carDetails = carDetailsService.getById(application.getCarDetailsId());
            final var photoOptional = getPhotoFromUpdate(update);
            if (photoOptional.isPresent()) {
                carDetails.setVinNumber(messageSource.getMessage("onPhoto"));
                carDetails.setVinNumberPhotoId(photoOptional.get().getFileId());
            } else {
                final var text = getTextFromUpdate(update);
                final var vinNumber = formatAndValidateVinNumber(text);
                carDetails.setVinNumber(vinNumber);
            }
            clientInfo.setState(INSERT_VIN_NUMBER);
            clientInfoService.update(clientInfo);
            carDetailsService.update(carDetails);
            sendMessages(update, clientInfo);
        } catch (IllegalArgumentException exception) {
            final var user = getUserFromUpdate(update);
            final var text = messageSource.getMessage("error.wrongVinNumberFormat");
            final var message = createTextMessageForUserWithRemoveKeyBoard(user.getId(), text);
            inomarkaStore.execute(message);
        }
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var text = messageSource.getMessage("insertMainPurpose");
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUser(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
