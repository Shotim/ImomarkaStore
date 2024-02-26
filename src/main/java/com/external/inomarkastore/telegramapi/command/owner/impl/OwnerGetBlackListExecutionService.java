package com.external.inomarkastore.telegramapi.command.owner.impl;

import com.external.inomarkastore.InomarkaStore;
import com.external.inomarkastore.model.ClientInfo;
import com.external.inomarkastore.service.ClientInfoService;
import com.external.inomarkastore.service.OwnerInfoService;
import com.external.inomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.inomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.inomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.stream.Stream;

import static com.external.inomarkastore.constant.OwnerState.BACK_FROM_BLACK_LIST;
import static com.external.inomarkastore.constant.OwnerState.GET_BLACK_LIST;
import static com.external.inomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButton;
import static com.external.inomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.inomarkastore.util.UpdateUtils.getUserFromUpdate;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class OwnerGetBlackListExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final EntitiesSendHelper entitiesSendHelper;

    @Override
    public String getCommand() {
        return GET_BLACK_LIST.name();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        final var user = getUserFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var blackListClients = clientInfoService.getBlackListClients();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        jsonDataObject.addProperty("receivedGetBlackListMessageId", messageIdFromUpdate);
        if (blackListClients.isEmpty()) {
            entitiesSendHelper.sendMessageForOwnerWithBackToMainMenuButton(
                    "owner.youDoNotHaveBlackListClients", user.getId(), jsonDataObject);
        } else {
            entitiesSendHelper.sendMessageForOwnerWithBackToMainMenuButton(
                    "owner.yourBlackListClients", user.getId(), jsonDataObject);
            for (ClientInfo clientInfo : blackListClients) {
                final var text = messageSource.getMessage("template.owner.clientInfo",
                        Stream.of(clientInfo.getName(), clientInfo.getPhoneNumber(), clientInfo.getTelegramUserName())
                                .map(string -> isBlank(string) ? EMPTY : string)
                                .toArray());
                final var buttonName = messageSource.getMessage("buttonName.owner.returnFromBlackList");
                final var clientInfoId = clientInfo.getId();
                final var callbackData = "%s:%s"
                        .formatted(BACK_FROM_BLACK_LIST.name(), clientInfoId);
                final var clientInfoMessage = createTextMessageForUserWithInlineButton(
                        user.getId(), text, buttonName, callbackData);
                final var clientInfoMessageId = inomarkaStore.execute(clientInfoMessage).getMessageId();
                jsonDataObject.addProperty(clientInfoId.toString(), clientInfoMessageId);
            }
        }
        ownerInfoService.updateState(GET_BLACK_LIST);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
    }
}
