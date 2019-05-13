package ru.geekbrains.server.command;

import ru.geekbrains.server.User;

import java.io.IOException;

public interface UserFactory {
    User actionsOfUser(String message) throws IOException;
}
