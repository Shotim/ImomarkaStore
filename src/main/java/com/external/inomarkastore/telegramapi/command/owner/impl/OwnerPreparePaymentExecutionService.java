package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.service.ApplicationService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.DeleteMessagesHelper;
import com.external.inomarkastore.util.BotMessageSource;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.inomarkastore.constant.OwnerState.PREPARE_PAYMENT;
import static com.external.inomarkastore.util.MessageUtils.createAnswerCallbackQuery;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.inomarkastore.util.UpdateUtils.getCallbackIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getLongIdFromCallbackDataFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerPreparePaymentExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ApplicationService applicationService;
    private final DeleteMessagesHelper deleteMessagesHelper;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return PREPARE_PAYMENT.name();
    }

    @Override
    @Transactional
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var applicationId = getLongIdFromCallbackDataFromUpdate(update);
        final var callbackIdFromUpdate = getCallbackIdFromUpdate(update);
        final var application = applicationService.getById(applicationId);
        final var callbackText = messageSource.getMessage("owner.callback.preparePayment");
        final var answerCallbackQuery = createAnswerCallbackQuery(callbackIdFromUpdate, callbackText);
        inomarkaStore.execute(answerCallbackQuery);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        deleteMessagesHelper.deleteAllMessagesFromJsonDataForUser(user.getId(), jsonDataObject);
        final var applicationPayloadForOwner = applicationService.getApplicationPayloadForOwner(application);
        final var enterPaymentSumText = messageSource.getMessage("owner.enterPaymentSum");
        final var text = applicationPayloadForOwner + "\n\n" + enterPaymentSumText;
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.backToApplications")
        );
        final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), text, buttonNames);
        final var sendMessageId = inomarkaStore.execute(sendMessage).getMessageId();
        final var jsonObject = new JsonObject();
        jsonObject.addProperty("applicationIdToPay:%s".formatted(application.getId()), sendMessageId);
        ownerInfoService.updateJsonData(jsonObject.toString());
        ownerInfoService.updateState(PREPARE_PAYMENT);
    }
}
