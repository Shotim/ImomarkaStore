package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.CarDetails;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.CarDetailsService;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.external.imomarkastore.constant.ClientState.DELETE_CAR;
import static com.external.imomarkastore.constant.ClientState.GET_CARS;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithButtonBackToMainMenu;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageWithInlineButton;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

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
    public void execute(Update update, ClientInfo clientInfo) {
        clientInfo.setState(GET_CARS);
        clientInfoService.update(clientInfo);
        sendMessages(update, clientInfo);
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var carDetailsList = carDetailsService.getActiveCarDetailsForClient(clientInfo);
        final var user = getUserFromUpdate(update);
        if (!carDetailsList.isEmpty()) {
            final var carDetailsListText = messageSource.getMessage("carDetailsList");
            final var message = createTextMessageWithButtonBackToMainMenu(user, carDetailsListText);
            inomarkaStore.execute(message);

            final var jsonObject = new JsonObject();
            for (CarDetails carDetails : carDetailsList) {
                final var carDetailsText = carDetailsService.getCarDetailsPayload(carDetails);
                final var haveOnlyArchivedApplications =
                        applicationService.getNotArchivedApplicationsForCar(carDetails).isEmpty();
                if (haveOnlyArchivedApplications) {
                    final var messageWithInlineButton =
                            createTextMessageWithInlineButton(user, carDetailsText,
                                    messageSource.getMessage("buttonName.client.deleteCar"),
                                    "%s:%s".formatted(DELETE_CAR.name(), carDetails.getId()));
                    final var executed = inomarkaStore.execute(messageWithInlineButton);
                    jsonObject.add(carDetails.getId().toString(), new JsonPrimitive(executed.getMessageId()));
                } else {
                    final var textMessageForUser = createTextMessageForUser(user, carDetailsText);
                    inomarkaStore.execute(textMessageForUser);
                }
            }
            clientInfo.setAdditionalJsonDataForNextOperations(jsonObject.toString());
            clientInfoService.update(clientInfo);
        } else {
            final var text = messageSource.getMessage("noActiveCarDetailsFound");
            final var message = createTextMessageWithButtonBackToMainMenu(user, text);
            inomarkaStore.execute(message);
            clientInfoService.update(clientInfo);
        }
    }
}
