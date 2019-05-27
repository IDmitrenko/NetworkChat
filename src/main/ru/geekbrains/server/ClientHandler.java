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

import static ru.geekbrains.client.MessagePatterns.*;

public class ClientHandler {

    private final String login;
    private final Socket socket;
    private final DataInputStream inp;
    private final DataOutputStream out;
    private ChatServer chatServer;
    private final ExecutorService executorService;
    private final Future<?> handlerFuture;

    private final BlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();

    public ClientHandler(String login, Socket socket,
                         ExecutorService executorService,
                         ChatServer chatServer) throws IOException {
        this.login = login;
        this.socket = socket;
        this.inp = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.executorService = executorService;
        this.chatServer = chatServer;

        this.handlerFuture = executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String text = inp.readUTF();
                        System.out.printf("Message from user %s: %s%n", login, text);

                        System.out.println("New message " + text);
                        TextMessage msg = parseTextMessageRegx(text, login);
                        if (msg != null) {
                            msg.swapUsers();
                            chatServer.sendMessage(msg);
                        } else if (text.equals(DISCONNECT)) {
                            System.out.printf("User %s is disconnected%n", login);
                            chatServer.unsubscribe(login);
                            return;
                        } else if (text.equals(USER_LIST_TAG)) {
                            System.out.printf("Sending user list to %s%n", login);
                            sendUserList(chatServer.getUserList());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        chatServer.unsubscribe(login);
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

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    String msg = null;
                    try {
                        msg =messageQueue.take();
                    } catch (InterruptedException ex) {
                        return;
                    }
                    if (socket.isConnected()) {
                        try {
                            out.writeUTF(msg);
                        } catch (IOException ex) {
                            return;
                        }
                    }
                }
            }
        });
    }

    public String getLogin() {
        return login;
    }

    public void sendMessage(String userFrom, String msg) throws IOException {
        if (socket.isConnected()) {
//            messageQueue.add(String.format(MESSAGE_SEND_PATTERN, userFrom, msg));
            if (!messageQueue.offer(String.format(MESSAGE_SEND_PATTERN, userFrom, msg)))
                out.writeUTF(MESSAGE_QUEUE_FULL);
        }
    }

    public void sendConnectedMessage(String login) throws IOException {
        if (socket.isConnected()) {
            out.writeUTF(String.format(CONNECTED_SEND, login));
        }
    }

    public void sendDisconnectedMessage(String login) throws IOException {
        if (socket.isConnected()) {
            out.writeUTF(String.format(DISCONNECT_SEND, login));
        }
    }

    public void sendUserList(Set<String> users) throws IOException {
        if (socket.isConnected()) {
            out.writeUTF(String.format(USER_LIST_RESPONSE, String.join(" ", users)));
        }
    }
}
