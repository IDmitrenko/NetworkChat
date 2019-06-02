package ru.geekbrains.server.command;

import ru.geekbrains.server.ClientHandler;
import ru.geekbrains.server.User;
import ru.geekbrains.server.auth.AuthService;

import java.io.DataOutputStream;
import java.util.Map;
import java.util.logging.Logger;

import static ru.geekbrains.client.MessagePatterns.AUTH_FAIL_RESPONSE;
import static ru.geekbrains.client.MessagePatterns.AUTH_SUCCESS_RESPONSE;

public class AuthCommand extends CommonCommand {
    private final AuthService authService;
    private static final Logger logger = Logger.getLogger(AuthCommand.class.getName());

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

