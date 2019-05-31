package ru.geekbrains.server.command;

import ru.geekbrains.server.ClientHandler;
import ru.geekbrains.server.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import static ru.geekbrains.client.MessagePatterns.AUTH_ALREADY_RESPONSE;
import static ru.geekbrains.server.ChatServer.logger;


public abstract class CommonCommand implements UserFactory {
    private final DataOutputStream out;
    private final Map<String, ClientHandler> clientHandlerMap;

    protected CommonCommand(DataOutputStream out,
                            Map<String, ClientHandler> clientHandlerMap) {
        this.out = out;
        this.clientHandlerMap = clientHandlerMap;
    }

    @Override
    public User actionsOfUser(String message) throws IOException {
        String[] regParts = message.split(" ");
        if (regParts.length != 3) {
            System.out.printf("Incorrect registration message %s%n", message);
            logger.warning("Неверное регистрационное сообщение: " + message);
            return sendFailResponse("Internal error");
        }
        User user = new User(0, regParts[1], regParts[2]);
        // Проверить, подключен ли уже пользователь. Если да, то отправить клиенту ошибку
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            if (clientHandler.getLogin().equals(regParts[1])) {
                System.out.printf("The user %s is already connected%n", regParts[1]);
                logger.warning("Пользователь " + regParts[1] + " уже подключен.");
                return sendResponse(AUTH_ALREADY_RESPONSE);
            }
        }
        if (actionWithUser(user)) {
            out.writeUTF(getSuccessResponse());
            out.flush();
            return user;
        } else {
            return sendFailResponse("");
        }
    }

    private User sendResponse(String msg) throws IOException {
        out.writeUTF(msg);
        out.flush();
        return null;
    }

    private User sendFailResponse(String msg) throws IOException {
        return sendResponse(getFailureResponse() + " " + msg);
    }

    protected abstract boolean actionWithUser(User user);

    protected abstract String getSuccessResponse();

    protected abstract String getFailureResponse();
}
