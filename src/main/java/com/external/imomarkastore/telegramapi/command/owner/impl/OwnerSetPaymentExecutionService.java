package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.imomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.SET_PAYMENT;
import static com.external.imomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.imomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getLongIdFromCallbackDataFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerSetPaymentExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ApplicationService applicationService;
    private final DeleteMessagesHelper deleteMessagesHelper;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return SET_PAYMENT.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var applicationId = getLongIdFromCallbackDataFromUpdate(update);
        final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
        final var application = applicationService.getById(applicationId);
        final var callbackText = messageSource.getMessage("owner.callback.setPayment");
        final var answerCallbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, callbackText);
        inomarkaStore.execute(answerCallbackQuery);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user.getId(), jsonDataObject);
        final var applicationPayloadForOwner = applicationService.getApplicationPayloadForOwner(application);
        final var enterConfirmSetPayment = messageSource.getMessage("owner.enterConfirmSetPayment");
        final var text = applicationPayloadForOwner + "\n\n" + enterConfirmSetPayment;
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.backToApplications")
        );
        final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), text, buttonNames);
        final var sendMessageId = inomarkaStore.execute(sendMessage).getMessageId();
        final var jsonObject = new JsonObject();
        jsonObject.addProperty("applicationIdToSetPayment:%s".formatted(application.getId()), sendMessageId);
        ownerInfoService.updateJsonData(jsonObject.toString());
        ownerInfoService.updateState(SET_PAYMENT);
    }
}
