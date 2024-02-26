package com.external.inomarkastore.telegramapi.message.client.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.constant.ClientState;
import com.external.inomarkastore.model.CarDetails;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.CarDetailsService;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.telegramapi.message.MessageExecutionService;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.constant.ClientState.DELETE_CAR;
import static com.external.inomarkastore.constant.ClientState.GET_CARS;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButton;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class GetCarsExecutionService implements MessageExecutionService {

    private final CarDetailsService carDetailsService;
    private final ClientInfoService clientInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return GET_CARS;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) throws TelegramApiException {
        clientInfo.setState(GET_CARS);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    public void sendMessages(Update update, ClientInfo clientInfo) throws TelegramApiException {
        final var carDetailsList = carDetailsService.getActiveCarDetailsForClient(clientInfo);
        final var user = getUserFromUpdate(update);
        if (!carDetailsList.isEmpty()) {
            final var carDetailsListText = messageSource.getMessage("carDetailsList");
            final var message = createTextMessageWithButtonBackToMainMenuForClient(user.getId(), carDetailsListText);
            inomarkaStore.execute(message);

            final var jsonObject = new JsonObject();
            for (CarDetails carDetails : carDetailsList) {
                final var carDetailsText = carDetailsService.getCarDetailsPayload(carDetails);
                final var haveOnlyArchivedApplications =
                        applicationService.getNotArchivedApplicationsForCar(carDetails).isEmpty();
                if (haveOnlyArchivedApplications) {
                    final var messageWithInlineButton =
                            createTextMessageForUserWithInlineButton(user.getId(), carDetailsText,
                                    messageSource.getMessage("buttonName.client.deleteCar"),
                                    "%s:%s".formatted(DELETE_CAR.name(), carDetails.getId()));
                    final var executed = inomarkaStore.execute(messageWithInlineButton);
                    jsonObject.add(carDetails.getId().toString(), new JsonPrimitive(executed.getMessageId()));
                } else {
                    final var textMessageForUser = createTextMessageForUser(user.getId(), carDetailsText);
                    inomarkaStore.execute(textMessageForUser);
                }
            }
            clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
            clientInfoService.update(clientInfo);
        } else {
            final var text = messageSource.getMessage("noActiveCarDetailsFound");
            final var message = createTextMessageWithButtonBackToMainMenuForClient(user.getId(), text);
            inomarkaStore.execute(message);
            clientInfoService.update(clientInfo);
        }
    }

    private SendMessage createTextMessageWithButtonBackToMainMenuForClient(Long telegramUserId, String text) {
        final var buttonNames = List.of(messageSource.getMessage("buttonName.client.backToMainMenu"));
        return createTextMessageForUserWithReplyKeyBoardMarkup(telegramUserId, text, buttonNames);
    }
}
