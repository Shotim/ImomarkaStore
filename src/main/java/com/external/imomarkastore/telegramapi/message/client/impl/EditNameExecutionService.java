package com.external.imomarkastore.telegramapi.message.client.impl;

import com.external.imomarkastore.InomarkaStore;
import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.external.imomarkastore.constant.ClientState.EDIT_NAME;
import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUserWithRemoveKeyBoard;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class EditNameExecutionService implements MessageExecutionService {

    private final ClientInfoService clientInfoService;
    private final InomarkaStore inomarkaStore;
    private final BotMessageSource messageSource;

    @Override
    public ClientState getState() {
        return EDIT_NAME;
    }

    @Override
    public void execute(Update update, ClientInfo clientInfo) {
        sendMessages(update, clientInfo);
    }

    @Override
    @SneakyThrows
    public void sendMessages(Update update, ClientInfo clientInfo) {
        final var clientInfoName = isBlank(clientInfo.getName()) ? EMPTY : clientInfo.getName();
        final var text = messageSource.getMessage("changeName",
                List.of(clientInfoName).toArray());
        clientInfo.setState(EDIT_NAME);
        clientInfoService.update(clientInfo);
        final var user = getUserFromUpdate(update);
        final var message = createTextMessageForUserWithRemoveKeyBoard(user.getId(), text);
        inomarkaStore.execute(message);
    }
}
