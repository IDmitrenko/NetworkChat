package ru.geekbrains.server.command;

import ru.geekbrains.server.ClientHandler;
import ru.geekbrains.server.User;
import ru.geekbrains.server.auth.AuthService;

import java.io.DataOutputStream;
import java.util.Map;

import static ru.geekbrains.client.MessagePatterns.AUTH_FAIL_RESPONSE;
import static ru.geekbrains.client.MessagePatterns.AUTH_SUCCESS_RESPONSE;
import static ru.geekbrains.server.ChatServer.logger;

public class AuthCommand extends CommonCommand {
    private final AuthService authService;

    protected AuthCommand(DataOutputStream out, AuthService authService, Map<String, ClientHandler> clientHandlerMap) {
        super(out, clientHandlerMap);
        this.authService = authService;
    }

    @Override
    protected boolean actionWithUser(User user) {
        if (authService.authUser(user)) {
            System.out.printf("User %s authorized successful!%n", user.getLogin());
            logger.info("Пользователь " + user.getLogin() + " авторизован.");
            return true;
        } else {
            logger.info("Пользователь " + user.getLogin() + " не авторизован.");
            return false;
        }
    }

    @Override
    protected String getSuccessResponse() {
        return AUTH_SUCCESS_RESPONSE;
    }

    @Override
    protected String getFailureResponse() {
        return AUTH_FAIL_RESPONSE;
    }
}

