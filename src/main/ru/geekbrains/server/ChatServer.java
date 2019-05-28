package ru.geekbrains.server;

import ru.geekbrains.client.TextMessage;
import ru.geekbrains.server.auth.AuthService;
import ru.geekbrains.server.auth.AuthServiceJdbcImpl;
import ru.geekbrains.server.command.CommandFactory;
import ru.geekbrains.server.command.UserFactory;
import ru.geekbrains.server.persistance.UserRepository;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class ChatServer {

    private AuthService authService;
    private Map<String, ClientHandler> clientHandlerMap = Collections.synchronizedMap(new HashMap<>());
//ctp    private ExecutorService executorService;
    private ExecutorService executorService = Executors.newFixedThreadPool(20,
        new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thr = Executors.defaultThreadFactory().newThread(r);
                thr.setDaemon(true);
                return thr;
            }
        });

    // ExecutorService с ограниченным числом потоков и ограниченной очередью заданий
    private ExecutorService limitedExecutorService = new ThreadPoolExecutor(20, 20,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100, true),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thr = Executors.defaultThreadFactory().newThread(r);
                    thr.setDaemon(true);
                    return thr;
                }
            });

    public static void main(String[] args) {
        AuthService authService;
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/network_chat" +
                            "?allowPublicKeyRetrieval=TRUE" +
//                            "?verifyServerCertificate=false" +
                            "&useSSL=false" +
                            "&requireSSL=false" +
                            "&useLegacyDatetimeCode=false" +
                            "&amp" +
                            "&serverTimezone=UTC",
                    "root", "DiasTopaz3922");
            UserRepository userRepository = new UserRepository(conn);
            authService = new AuthServiceJdbcImpl(userRepository);
        } catch (SQLException e) {
            e.printStackTrace();
            // лог ошибки
            return;
        }

        ChatServer chatServer = new ChatServer(authService);
        chatServer.start(7777);
    }

    public ChatServer(AuthService authService) {
        this.authService = authService;
//ctp        this.executorService = Executors.newCachedThreadPool();
    }

/*ctp
    public ExecutorService getExecutorService() {
        return executorService;
    }
*/

    private void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started!");
            // лог  - Сервер запущен
            while (true) {
                Socket socket = serverSocket.accept();
                DataInputStream inp = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                System.out.println("New client connected!");
                // лог клиент попытка подключения

                User user = null;
                try {
                    String authMessage = inp.readUTF();
                    UserFactory userFactory = CommandFactory.valueOf(authMessage, out, authService, clientHandlerMap);
                    user = userFactory.actionsOfUser(authMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // лог ошибка аутентификации или добавления нового пользователя
                }

                if (user != null) {
                    subscribe(user.getLogin(), socket);
                } else {
                    socket.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // лог ошибка
        }
    }

    private void sendUserConnectedMessage(String login) throws IOException {
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            if (!clientHandler.getLogin().equals(login)) {
                System.out.printf("Sending connect notification to %s about %s%n", clientHandler.getLogin(), login);
                // лог  подключение конкретного клиента прошло успешно
                clientHandler.sendConnectedMessage(login);
            }
        }
    }

    private void sendUserDisconnectedMessage(String login) throws IOException {
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            if (!clientHandler.getLogin().equals(login)) {
                System.out.printf("Sending disconnect notification to %s about %s%n", clientHandler.getLogin(), login);
                // лог отключение конкретного клиента
                clientHandler.sendDisconnectedMessage(login);
            }
        }
    }

    public void sendMessage(TextMessage msg) throws IOException {
        ClientHandler userToClientHandler = clientHandlerMap.get(msg.getUserTo());
        if (userToClientHandler != null) {
            // лог клиент прислал сообщение/команду
            userToClientHandler.sendMessage(msg.getUserFrom(), msg.getText());
        } else {
            System.out.printf("User %s not connected%n", msg.getUserTo());
        }
    }

    public Set<String> getUserList() {
        return Collections.unmodifiableSet(clientHandlerMap.keySet());
    }

    public boolean subscribe(String login, Socket socket) throws IOException {
        clientHandlerMap.put(login, new ClientHandler(login, socket, limitedExecutorService, this));
        sendUserConnectedMessage(login);
        return true;
    }

    public void unsubscribe(String login) {
        clientHandlerMap.remove(login);
        try {
            sendUserDisconnectedMessage(login);
        } catch (IOException e) {
            System.err.println("Error sending disconnect message");
            e.printStackTrace();
        }
    }
}
