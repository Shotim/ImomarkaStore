package com.external.imomarkastore;

import com.external.imomarkastore.constant.ClientState;
import com.external.imomarkastore.model.ClientInfo;
import com.external.imomarkastore.service.ClientInfoService;
import com.external.imomarkastore.telegramapi.command.CommandExecutionService;
import com.external.imomarkastore.telegramapi.message.MessageExecutionService;
import com.external.imomarkastore.util.BotMessageSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.external.imomarkastore.util.MessageUtils.createTextMessageForUser;
import static com.external.imomarkastore.util.UpdateUtils.getTextFromUpdate;
import static com.external.imomarkastore.util.UpdateUtils.getUserFromUpdate;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

@Slf4j
@Component
public class InomarkaStore extends TelegramLongPollingBot {

    private final String botName;
    private final Map<ClientState, List<ClientState>> stateMatrix;
    private final Map<String, ClientState> buttonClientToStateMatrix;
    private final ClientInfoService clientInfoService;
    private Map<String, CommandExecutionService> clientCommandExecutionServicesByCommands;
    private final List<ClientState> callbackExecutionClientStates;
    private final List<ClientState> messageExecutionClientStates;
    private Map<ClientState, MessageExecutionService> messageExecutionServicesByClientState;
    private final BotMessageSource messageSource;

    public InomarkaStore(@Value("${bot.token}") String botToken,
                         @Value("${bot.name}") String botName,
                         @Qualifier("stateMatrix") Map<ClientState, List<ClientState>> stateMatrix,
                         ClientInfoService clientInfoService,
                         @Qualifier("buttonToClientStateMatrix") Map<String, ClientState> buttonClientToStateMatrix,
                         @Qualifier("callbackExecutionClientStates") List<ClientState> callbackExecutionClientStates,
                         @Qualifier("messageExecutionClientStates") List<ClientState> messageExecutionClientStates,
                         BotMessageSource messageSource) {
        super(botToken);
        this.botName = botName;
        this.stateMatrix = stateMatrix;
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

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final var user = getUserFromUpdate(update);
        final var clientInfoOptional = clientInfoService.getByTelegramUserId(user.getId());
        if (update.hasCallbackQuery()) {
            processCallBacks(update, clientInfoOptional);
        } else {
            processMessagesAndCommands(update, clientInfoOptional);
        }
    }

    private void processMessagesAndCommands(Update update, Optional<ClientInfo> clientInfoOptional) {
        final var text = getTextFromUpdate(update);
        final var commandExecutionService = clientCommandExecutionServicesByCommands.get(text);
        if (nonNull(commandExecutionService)) {
            commandExecutionService.execute(update);
        } else {
            processMessages(update, clientInfoOptional, text);
        }
    }

    private void processMessages(Update update, Optional<ClientInfo> clientInfoOptional, String text) {
        final var user = getUserFromUpdate(update);
        if (clientInfoOptional.isPresent()) {
            final var clientInfo = clientInfoOptional.get();
            final var clientInfoState = clientInfo.getState();
            final var nextStates = stateMatrix.get(clientInfoState);
            final var messageNextStates = nextStates.stream()
                    .filter(messageExecutionClientStates::contains).toList();
            final var buttonState = isNotBlank(text) ? buttonClientToStateMatrix.get(text) : null;
            if (nonNull(buttonState) && !messageNextStates.contains(buttonState)) {
                sendErrorMessage(update, nextStates);
                return;
            }
            final var clientState = isNotEmpty(messageNextStates) && messageNextStates.size() == 1 ?
                    messageNextStates.get(0) :
                    buttonState;
            final var messageExecutionService = messageExecutionServicesByClientState.get(clientState);
            if (nonNull(messageExecutionService)) {
                messageExecutionService.execute(update, clientInfo);
            } else {
                final var errorText = messageSource.getMessage("error.botConfig");
                sendErrorMessage(user, errorText);
            }
        } else {
            final var errorText = messageSource.getMessage("error.noClientInfoForAction");
            sendErrorMessage(user, errorText);
        }
    }

    private void processCallBacks(Update update, Optional<ClientInfo> clientInfoOptional) {
        final var data = update.getCallbackQuery().getData();
        final var callbackState = data.substring(0, data.indexOf(":"));
        if (clientInfoOptional.isPresent()) {
            final var clientInfo = clientInfoOptional.get();
            final var clientInfoState = clientInfo.getState();
            final var nextStates = stateMatrix.get(clientInfoState);
            final var callbackNextStates = nextStates.stream()
                    .filter(callbackExecutionClientStates::contains)
                    .toList();
            final var nextState = ClientState.valueOf(callbackState);
            if (!callbackNextStates.contains(nextState)) {
                sendErrorMessage(update, nextStates);
            } else {
                final var messageExecutionService = messageExecutionServicesByClientState.get(nextState);
                messageExecutionService.execute(update, clientInfo);
            }
        } else {
            final var user = getUserFromUpdate(update);
            final var errorText = messageSource.getMessage("error.noClientInfoForAction");
            sendErrorMessage(user, errorText);
        }
    }

    @SneakyThrows
    private void sendErrorMessage(User user, String errorText) {
        final var errorMessage = createTextMessageForUser(user, errorText);
        this.execute(errorMessage);
    }


    private void sendErrorMessage(Update update, List<ClientState> nextStates) {
        log.error("Wrong command");
        final var user = getUserFromUpdate(update);
        final var availableStates = nextStates.stream()
                .filter(Objects::nonNull)
                .map(ClientState::getClientStateText)
                .collect(joining(", "));
        final var errorText = messageSource.getMessage("error.nextStates", List.of(availableStates).toArray());
        sendErrorMessage(user, errorText);
    }
}
