package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.PREPARE_DISTRIBUTION;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithReplyKeyBoardMarkup;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerPrepareDistributionExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public String getCommand() {
        return PREPARE_DISTRIBUTION.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        jsonDataObject.addProperty("receivedPrepareDistributionMessageId", messageIdFromUpdate);
        final var text = messageSource.getMessage("owner.prepareDistribution");
        final var buttonNames = List.of(
                messageSource.getMessage("buttonName.owner.backToMainMenu")
        );
        final var sendMessage = createTextMessageForUserWithReplyKeyBoardMarkup(user.getId(), text, buttonNames);
        final var sendMessageId = inomarkaStore.execute(sendMessage).getMessageId();
        jsonDataObject.addProperty("prepareDistributionMessageId", sendMessageId);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
        ownerInfoService.updateState(PREPARE_DISTRIBUTION);
    }
}
