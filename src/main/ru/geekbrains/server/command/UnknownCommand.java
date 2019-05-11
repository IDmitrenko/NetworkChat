package ru.geekbrains.server.command;

import ru.geekbrains.server.User;

import java.io.DataOutputStream;
import java.io.IOException;

public class UnknownCommand implements UserFactory {
    private final DataOutputStream out;

    public UnknownCommand(DataOutputStream out) {
        this.out = out;
    }

    @Override
    public User createUser(String message) throws IOException {
        System.out.println("Internal error : Unknown command - " + message);
        out.writeUTF("Unknown command: " + message);
        out.flush();
        return null;
    }
}
