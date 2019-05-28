package ru.geekbrains.server;


import java.util.concurrent.Future;

public class FutureHandler {
    private final Future<?> commandFuture;
    private final Future<?> messageFuture;

    public FutureHandler(Future<?> commandFuture, Future<?> messageFuture) {
        this.commandFuture = commandFuture;
        this.messageFuture = messageFuture;
    }

    public Future<?> getCommandFuture() {
        return commandFuture;
    }

    public Future<?> getMessageFuture() {
        return messageFuture;
    }
}