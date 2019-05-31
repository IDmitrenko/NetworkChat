package ru.geekbrains.server.command;

import ru.geekbrains.server.ClientHandler;
import ru.geekbrains.server.User;
import ru.geekbrains.server.auth.AuthService;

import java.io.DataOutputStream;
import java.util.Map;

import static ru.geekbrains.client.MessagePatterns.REGISTRATION_FAIL_RESPONSE;
import static ru.geekbrains.client.MessagePatterns.REGISTRATION_SUCCESS_RESPONSE;
import static ru.geekbrains.server.ChatServer.logger;

public class RegistrationCommand extends CommonCommand {

    private final AuthService authService;

    public RegistrationCommand(DataOutputStream out, AuthService authService, Map<String, ClientHandler> clientHandlerMap) {
        super(out, clientHandlerMap);
        this.authService = authService;
    }

    @Override
    protected boolean actionWithUser(User user) {
        if (authService.createUser(user)) {
            System.out.printf("User %s added successfully!%n", user.getLogin());
            logger.info("Пользователь " + user.getLogin() + " успешно авторизован!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected String getSuccessResponse() {
        return REGISTRATION_SUCCESS_RESPONSE;
    }

    @Override
    protected String getFailureResponse() {
        return REGISTRATION_FAIL_RESPONSE;
    }
}
