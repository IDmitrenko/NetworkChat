package ru.geekbrains.client;

import ru.geekbrains.client.history.UserHistory;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;

import static ru.geekbrains.client.MessagePatterns.*;

public class Network implements Closeable {

    private Socket socket;
    private DataInputStream in;
    public DataOutputStream out;

    private String hostName;
    private int port;
    private MessageReciever messageReciever;

    private String login;

    private Thread receiverThread;


    public Network(String hostName, int port, MessageReciever messageReciever) {
        this.hostName = hostName;
        this.port = port;
        this.messageReciever = messageReciever;

        this.receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String text = in.readUTF();

                        System.out.println("New message " + text);
                        TextMessage msg = parseTextMessageRegx(text, login);
                        if (msg != null) {
                            messageReciever.submitMessage(msg);
                            continue;
                        }

                        String login = parseConnectedMessage(text);
                        if (login != null) {
                            messageReciever.userConnected(login);
                            continue;
                        }

                        login = parseDisconnectedMessage(text);
                        if (login != null) {
                            messageReciever.userDisconnected(login);
                            continue;
                        }

                        Set<String> users = parseUserList(text);
                        if (users != null) {
                            messageReciever.updateUserList(users);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (socket.isClosed()) {
                            break;
                        }
                    }
                }
            }
        });
    }

    public void authorize(String login, String password) throws IOException, AuthException {
        connectToServer(login, AUTH_PATTERN, password, AUTH_SUCCESS_RESPONSE);
/*
        List<TextMessage> messageHistory = userHistory.listHistory(50);
        if (!messageHistory.isEmpty()) {
            for (TextMessage textMessage : messageHistory) {
                messageReciever.submitMessage(textMessage);
            }
        }
*/
    }

    public void newUserRegistration(String login, String password) throws IOException, AuthException {
        connectToServer(login, REGISTRATION_PATTERN, password, REGISTRATION_SUCCESS_RESPONSE);
    }

    private void connectToServer(String login, String authPattern, String password, String authSuccessResponse) throws IOException, AuthException {
        socket = new Socket(hostName, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        sendMessage(String.format(authPattern, login, password));
        String response = in.readUTF();
        if (response.equals(authSuccessResponse)) {
            this.login = login;
            receiverThread.start();
        } else if (response.equals(AUTH_ALREADY_RESPONSE)) {
            TextMessage msg = new TextMessage(login, login, "The user is already connected");
            messageReciever.submitMessage(msg);
        } else {
            throw new AuthException(response);
        }
    }

    public void sendTextMessage(TextMessage message) {
        sendMessage(String.format(MESSAGE_SEND_PATTERN, message.getUserTo(), message.getText()));
    }

    private void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestConnectedUserList() {
        sendMessage(USER_LIST_TAG);
    }

    public String getLogin() {
        return login;
    }

    @Override
    public void close() {
        this.receiverThread.interrupt();
        sendMessage(DISCONNECT);
    }
}
