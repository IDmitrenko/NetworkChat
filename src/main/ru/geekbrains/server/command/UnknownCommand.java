package ru.geekbrains.server.command;

import ru.geekbrains.server.User;

import java.io.DataOutputStream;
import java.io.IOException;

import static ru.geekbrains.server.ChatServer.logger;

public class UnknownCommand implements UserFactory {
    private final DataOutputStream out;

    public UnknownCommand(DataOutputStream out) {
        this.out = out;
    }

    @Override
    public User actionsOfUser(String message) throws IOException {
        System.out.println("Internal error : Unknown command - " + message);
        logger.warning("Внутренняя ошибка : неверная команда - " + message);
        out.writeUTF("Unknown command: " + message);
        out.flush();
        return null;
    }
}
