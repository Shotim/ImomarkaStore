package com.external.imomarkastore;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.exception.BotException;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

@Component
@Slf4j
public class InomarkaStoreClient {

    private final Map<ClientState, List<ClientState>> clientStateMatrix;
    private final Map<String, ClientState> buttonClientToStateMatrix;
    private Map<String, CommandExecutionService> clientCommandExecutionServicesByCommands;
    private final List<ClientState> callbackExecutionClientStates;
    private final List<ClientState> messageExecutionClientStates;
    private Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState;
    private final ClientInfoService clientInfoService;
    private final BotMessageSource messageSource;

    public InomarkaStoreClient(
            ClientInfoService clientInfoService,
            BotMessageSource messageSource,
            @Qualifier("clientStateMatrix") Map<ClientState, List<ClientState>> clientStateMatrix,
            @Qualifier("buttonToClientStateMatrix") Map<String, ClientState> buttonClientToStateMatrix,
            @Qualifier("callbackExecutionClientStates") List<ClientState> callbackExecutionClientStates,
            @Qualifier("messageExecutionClientStates") List<ClientState> messageExecutionClientStates) {
        this.clientStateMatrix = clientStateMatrix;
        this.clientInfoService = clientInfoService;
        this.buttonClientToStateMatrix = buttonClientToStateMatrix;
        this.callbackExecutionClientStates = callbackExecutionClientStates;
        this.messageExecutionClientStates = messageExecutionClientStates;
        this.messageSource = messageSource;
    }

    @Autowired
    @Lazy
    public void setClientCommandExecutionServicesByCommands(
            @Qualifier("clientCommandExecutionServicesByCommands")
            Map<String, CommandExecutionService> clientCommandExecutionServicesByCommands) {
        this.clientCommandExecutionServicesByCommands = clientCommandExecutionServicesByCommands;
    }

    @Autowired
    @Lazy
    public void setMessageExecutionServicesByClientState(
            @Qualifier("messageExecutionServicesByClientState")
            Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState) {
        this.messageExecutionServicesByClientState = messageExecutionServicesByClientState;
    }

    public void processAction(Update update, User user) {
        final var clientInfoOptional = clientInfoService.getByTelegramUserId(user.getId());
        if (clientInfoOptional.isPresent() && TRUE.equals(clientInfoOptional.get().getIsInBlackList())) {
            final var text = messageSource.getMessage("youBlackListed");
            throwException(user, text);
        } else {
            if (update.hasCallbackQuery()) {
                processClientCallBacks(update, clientInfoOptional);
            } else {
                processClientMessagesAndCommands(update, clientInfoOptional);
            }
        }
    }

    private void processClientMessagesAndCommands(Update update, Optional<ClientInfo> clientInfoOptional) {
        final var text = getTextFromUpdate(update);
        final var commandExecutionService = clientCommandExecutionServicesByCommands.get(text);
        if (nonNull(commandExecutionService)) {
            commandExecutionService.execute(update);
        } else {
            processClientMessages(update, clientInfoOptional, text);
        }
    }

    private void processClientMessages(Update update, Optional<ClientInfo> clientInfoOptional, String text) {
        final var user = getUserFromUpdate(update);
        if (clientInfoOptional.isPresent()) {
            final var clientInfo = clientInfoOptional.get();
            final var clientInfoState = clientInfo.getState();
            final var nextStates = clientStateMatrix.get(clientInfoState);
            final var messageNextStates = nextStates.stream()
                    .filter(messageExecutionClientStates::contains).toList();
            final var buttonState = isNotBlank(text) ? buttonClientToStateMatrix.get(text) : null;
            if (nonNull(buttonState) && !messageNextStates.contains(buttonState)) {
                throwException(update, nextStates);
            }
            final var clientState = isNotEmpty(messageNextStates) && messageNextStates.size() == 1 ?
                    messageNextStates.get(0) :
                    buttonState;
            final var messageExecutionService = messageExecutionServicesByClientState.get(clientState);
            if (nonNull(messageExecutionService)) {
                messageExecutionService.execute(update, clientInfo);
            } else {
                final var errorText = messageSource.getMessage("error.botConfig");
                throwException(user, errorText);
            }
        } else {
            final var errorText = messageSource.getMessage("error.noClientInfoForAction");
            throwException(user, errorText);
        }
    }

    private void processClientCallBacks(Update update, Optional<ClientInfo> clientInfoOptional) {
        final var data = update.getCallbackQuery().getData();
        final var callbackState = data.substring(0, data.indexOf(":"));
        if (clientInfoOptional.isPresent()) {
            final var clientInfo = clientInfoOptional.get();
            final var clientInfoState = clientInfo.getState();
            final var nextStates = clientStateMatrix.get(clientInfoState);
            final var callbackNextStates = nextStates.stream()
                    .filter(callbackExecutionClientStates::contains)
                    .toList();
            final var nextState = ClientState.valueOf(callbackState);
            if (!callbackNextStates.contains(nextState)) {
                throwException(update, nextStates);
            } else {
                final var messageExecutionService = messageExecutionServicesByClientState.get(nextState);
                messageExecutionService.execute(update, clientInfo);
            }
        } else {
            final var user = getUserFromUpdate(update);
            final var errorText = messageSource.getMessage("error.noClientInfoForAction");
            throwException(user, errorText);
        }
    }

    private void throwException(User user, String errorText) {
        final var errorMessage = createTextMessageForUser(user, errorText);
        throw new BotException(errorMessage);
    }


    private void throwException(Update update, List<ClientState> nextStates) {
        log.error("Wrong command");
        final var user = getUserFromUpdate(update);
        final var availableStates = nextStates.stream()
                .filter(Objects::nonNull)
                .map(ClientState::getClientStateText)
                .collect(joining(", "));
        final var errorText = messageSource.getMessage("error.nextStates", List.of(availableStates).toArray());
        throwException(user, errorText);
    }
}
