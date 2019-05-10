package ru.geekbrains.server.command;

import ru.geekbrains.server.ClientHandler;
import ru.geekbrains.server.auth.AuthService;

import java.io.DataOutputStream;
import java.util.Map;

import static ru.geekbrains.client.MessagePatterns.REGISTRATION_TAG;

public class CommandFactory {
    public static UserFactory valueOf(String msg, DataOutputStream out, AuthService authService, Map<String, ClientHandler> clientHandlerMap) {
        if (msg.contains(REGISTRATION_TAG)) {
            return new RegistrationCommand(out,authService, clientHandlerMap);
        } else {
            return new AuthCommand(out,authService, clientHandlerMap);
        }
    }
}
