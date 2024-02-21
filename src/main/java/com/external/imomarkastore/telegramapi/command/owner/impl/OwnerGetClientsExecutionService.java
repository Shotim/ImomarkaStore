package com.external.imomarkastore.telegramapi.command.owner.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.service.OwnerInfoService;
import com.external.imomarkastore.telegramapi.command.owner.OwnerActionExecuteService;
import com.external.imomarkastore.telegramapi.command.owner.common.EntitiesSendHelper;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.external.imomarkastore.constant.OwnerState.GET_CLIENTS;
import static com.external.imomarkastore.constant.OwnerState.MOVE_CLIENT_TO_BLACK_LIST;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithInlineButton;
import static com.external.imomarkastore.util.UpdateUtils.getMessageIdFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;

@Service
@RequiredArgsConstructor
public class OwnerGetClientsExecutionService implements OwnerActionExecuteService {

    private final OwnerInfoService ownerInfoService;
    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;
    private final EntitiesSendHelper entitiesSendHelper;

    @Override
    public String getCommand() {
        return GET_CLIENTS.name();
    }

    @Override
    @SneakyThrows
    public void execute(Update update) {
        final var user = getUserFromUpdate(update);
        final var messageIdFromUpdate = getMessageIdFromUpdate(update);
        final var activeClients = clientInfoService.getActiveClients();
        final var jsonDataObject = ownerInfoService.getJsonDataObject();
        jsonDataObject.addProperty("receivedGetClientsMessageId", messageIdFromUpdate);
        if (activeClients.isEmpty()) {
            entitiesSendHelper.sendMessageForOwnerWithBackToMainMenuButton(
                    "owner.youDoNotHaveActiveClients", user, jsonDataObject);
        } else {
            entitiesSendHelper.sendMessageForOwnerWithBackToMainMenuButton(
                    "owner.yourActiveClients", user, jsonDataObject);
            for (ClientInfo clientInfo : activeClients) {
                final var text = messageSource.getMessage("template.owner.clientInfo",
                        List.of(clientInfo.getName(), clientInfo.getPhoneNumber()).toArray());
                final var buttonName = messageSource.getMessage("buttonName.owner.moveToBlackList");
                final var clientInfoId = clientInfo.getId();
                final var callbackData = "%s:%s"
                        .formatted(MOVE_CLIENT_TO_BLACK_LIST.name(), clientInfoId);
                final var clientInfoMessage = createTextMessageForUserWithInlineButton(
                        user.getId(), text, buttonName, callbackData);
                final var clientInfoMessageId = inomarkaStore.execute(clientInfoMessage).getMessageId();
                jsonDataObject.addProperty(clientInfoId.toString(), clientInfoMessageId);
            }
        }
        ownerInfoService.updateState(GET_CLIENTS);
        ownerInfoService.updateJsonData(jsonDataObject.toString());
    }
}
