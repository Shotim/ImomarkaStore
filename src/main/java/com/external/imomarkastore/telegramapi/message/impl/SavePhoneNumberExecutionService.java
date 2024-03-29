package com.external.imomarkastore.telegramapi.message.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.ClientState.MAIN_MENU;
import static com.external.imomarkastore.constant.ClientState.SAVE_PHONE_NUMBER;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithReplyKeyboardForMainMenu;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static com.external.imomarkastore.util.ValidationUtils.formatAndValidatePhoneNumber;

@Service
@RequiredArgsConstructor
public class SavePhoneNumberExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final CarDetailsService carDetailsService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return SAVE_PHONE_NUMBER;
    }

    @Override
    @SneakyThrows
    @Transactional
    public void execute(Update update, ClientInfo clientInfo) {
        try {
            final var text = getTextFromUpdate(update);
            final var formattedPhoneNumber = formatAndValidatePhoneNumber(text);
            final var applicationsForClient = applicationService.getApplicationsForClient(clientInfo);
            applicationsForClient.forEach(application -> application.setPhoneNumber(formattedPhoneNumber));
            applicationService.updateAll(applicationsForClient);
            final var carDetails = carDetailsService.getActiveCarDetailsForClient(clientInfo);
            carDetails.forEach(carDetail -> carDetail.setPhoneNumber(formattedPhoneNumber));
            carDetailsService.updateAll(carDetails);
            clientInfo.setPhoneNumber(formattedPhoneNumber);
            clientInfo.setState(MAIN_MENU);
            clientInfoService.update(clientInfo);
            sendMessages(update, clientInfo);
        } catch (IllegalArgumentException exception) {
            final var user = getUserFromUpdate(update);
            final var text = messageSource.getMessage("error.wrongVinNumberFormat");
            final var message = createTextMessageForUserWithRemoveKeyBoard(user, text);
            inomarkaStore.execute(message);
        }
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var user = getUserFromUpdate(update);
        final var text = messageSource.getMessage("phoneNumberSavedSuccessfully");
        final var message = createTextMessageWithReplyKeyboardForMainMenu(user, text);
        inomarkaStore.execute(message);
    }
}
