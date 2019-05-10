package ru.geekbrains.server.command;

import ru.geekbrains.server.User;

import java.io.IOException;

public interface UserFactory {
    User createUser(String message) throws IOException;
}
