package ru.geekbrains.server;

import ru.geekbrains.client.TextMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.geekbrains.client.MessagePatterns.*;

public class ClientHandler {

    private final String login;
    private final Socket socket;
    private final DataInputStream inp;
    private final DataOutputStream out;
    private final BlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
    private final FutureHandler futureHandler;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(String login, Socket socket,
                         ExecutorService executorService,
                         ChatServer chatServer) throws IOException {
        this.login = login;
        this.socket = socket;
        this.inp = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        Future<?> handlerFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String text = inp.readUTF();
                        System.out.printf("Message from user %s: %s%n", login, text);
                        logger.info("Пришло сообщение от пользователя " + login + " : " + text);
                        System.out.println("New message " + text);
                        TextMessage msg = parseTextMessageRegx(text, login);
                        if (msg != null) {
                            msg.swapUsers();
                            chatServer.sendMessage(msg);
                        } else if (text.equals(DISCONNECT)) {
                            System.out.printf("User %s is disconnected%n", login);
                            logger.info("Пришла команда на отключение пользователя " + login);
                            closeClient(chatServer, login);
                            return;
                        } else if (text.equals(USER_LIST_TAG)) {
                            System.out.printf("Sending user list to %s%n", login);
                            logger.fine("Пришла команда на выдачу списка активных пользователей");
                            sendUserList(chatServer.getUserList());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        closeClient(chatServer, login);
                        logger.log(Level.SEVERE, "Сбой потока чтения на сервере для пользователя "+ login, e);
                        break;
                    }
                }
            }
        });
        // обработка операций с объектом Future
//        handlerFuture.isDone();
//        handlerFuture.get();
//        обработка ошибки (executorService.shutdown())

//ctp        chatServer.getExecutorService().execute(new Runnable() {

        Future<?> messageFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    String msg = null;
                    try {
                        msg = messageQueue.take();
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE,
                                "Сбой при обработке сообщения из очереди для пользователя " + login +".",
                                ex);
                        return;
                    }
                    if (socket.isConnected()) {
                        try {
                            out.writeUTF(msg);
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE,
                                    "Сбой при отправке сообщения пользователя " + login,
                                    ex);
                            return;
                        }
                    }
                }
            }
        });
        futureHandler = new FutureHandler(handlerFuture, messageFuture);
    }

    private void closeClient(ChatServer chatServer, String login) {
        chatServer.unsubscribe(login);
        futureHandler.getMessageFuture().cancel(true);
        futureHandler.getCommandFuture().cancel(true);
    }

    public String getLogin() {
        return login;
    }

    public void sendMessage(String userFrom, String msg) throws IOException {
        if (socket.isConnected()) {
//            messageQueue.add(String.format(MESSAGE_SEND_PATTERN, userFrom, msg));
            if (!messageQueue.offer(String.format(MESSAGE_SEND_PATTERN, userFrom, msg))) {
                out.writeUTF(MESSAGE_QUEUE_FULL);
                logger.warning("Переполнена очередь сообщений пользователя " + this.login);
            }

        }
    }

    public void sendConnectedMessage(String login) throws IOException {
        if (socket.isConnected()) {
            out.writeUTF(String.format(CONNECTED_SEND, login));
            logger.fine("Отправили сообщение о присоединении к чату пользователя " + login);
        }
    }

    public void sendDisconnectedMessage(String login) throws IOException {
        if (socket.isConnected()) {
            out.writeUTF(String.format(DISCONNECT_SEND, login));
            logger.fine("Отправили сообщение об отключении от чата пользователя " + login);
        }
    }

    public void sendUserList(Set<String> users) throws IOException {
        if (socket.isConnected()) {
            out.writeUTF(String.format(USER_LIST_RESPONSE, String.join(" ", users)));
            logger.fine("Отправили пользователю " + login + " список активных пользователей.");
        }
    }
}
