package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.ApplicationService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.SEND_PAYMENT;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class OwnerSendPaymentExecutionService implements OwnerActionExecuteService {
    private final OwnerInfoService ownerInfoService;
    private final ApplicationService applicationService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return SEND_PAYMENT.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var text = getTextFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        final var applicationIdToPayOptional = jsonDataObject.keySet().stream()
                .filter(key -> key.startsWith("applicationIdToPay"))
                .findFirst();

        if (applicationIdToPayOptional.isPresent()) {
            final var applicationIdString = applicationIdToPayOptional.get();
            final var applicationId = Long.valueOf(applicationIdString.substring(applicationIdString.indexOf(":") + 1));
            final var applicationOptional = applicationService.getById(applicationId);
            if (applicationOptional.isPresent()) {
                final var application = applicationOptional.get();
                final var telegramUserId = application.getTelegramUserId();
                if (nonNull(telegramUserId)) {
                    jsonDataObject.addProperty("receivedSendPaymentMessageId", messageIdFromUpdate);
                    application.setSentRequestForPayment(true);
                    final var title = messageSource.getMessage("owner.paymentTitle");
                    final var description = messageSource.getMessage("owner.paymentDescription", List.of(applicationId).toArray());
                    final var priceAsDouble = Double.parseDouble(text);
                    final var priceValue = Double.valueOf(priceAsDouble * 100).intValue();
                    final var priceLabel = "1";
                    //TODO
//                    final var sendInvoice = createSendInvoice(telegramUserId, title, description, priceValue, priceLabel);
//                    inomarkaStore.execute(sendInvoice);
                    final var paymentSentMessage = messageSource.getMessage("owner.paymentSent");
                    final var buttonNames = List.of(
                            messageSource.getMessage("buttonName.owner.backToApplications"),
                            messageSource.getMessage("buttonName.owner.backToMainMenu")
                    );
                    final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), paymentSentMessage, buttonNames);
                    final var sentMessageId = inomarkaStore.execute(sendMessage).getMessageId();
                    jsonDataObject.addProperty("sentSendPaymentMessageId", sentMessageId);
                    applicationService.update(application);
                    ownerInfoService.updateState(SEND_PAYMENT);
                    ownerInfoService.updateJsonData(jsonDataObject.toString());
                }
            }
        }
    }
}
