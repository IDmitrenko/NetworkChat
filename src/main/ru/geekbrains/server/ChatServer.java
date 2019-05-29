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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.*;

public class ChatServer {

    public static final int MAXIMUM_POOL_SIZE = 20;
    public static final int CAPACITY = 80;
    private AuthService authService;
    private Map<String, ClientHandler> clientHandlerMap = new ConcurrentHashMap<>();
    public static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    //ctp    private ExecutorService executorService;
    /*private ExecutorService executorService = Executors.newFixedThreadPool(20,
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thr = Executors.defaultThreadFactory().newThread(r);
                    thr.setDaemon(true);
                    return thr;
                }
            });*/

    // ExecutorService с ограниченным числом потоков и ограниченной очередью заданий
    private ExecutorService limitedExecutorService = new ThreadPoolExecutor(MAXIMUM_POOL_SIZE, MAXIMUM_POOL_SIZE,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(CAPACITY, true),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thr = Executors.defaultThreadFactory().newThread(r);
                    thr.setDaemon(true);
                    return thr;
                }
            });

    public static void main(String[] args) throws IOException {
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

        LogManager.getLogManager().readConfiguration(ChatServer.class.getClassLoader()
                .getResourceAsStream("jul.properties"));

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
            logger.info("Server started!");

            while (true) {
                Socket socket = serverSocket.accept();
                DataInputStream inp = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                System.out.println("New client connected!");
                logger.fine("New client connected!");

                if (clientHandlerMap.size() * 2 >= MAXIMUM_POOL_SIZE) {
                    out.writeUTF("Thread pool is overfull, please wait until somebody disconnect and retry connect!!!");
                    logger.warning("Thread pool is overfull, please wait until somebody disconnect and retry connect!!!");
                    continue;
                }
                User user = null;
                try {
                    String authMessage = inp.readUTF();
                    UserFactory userFactory = CommandFactory.valueOf(authMessage, out, authService, clientHandlerMap);
                    user = userFactory.actionsOfUser(authMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    logger.log(Level.SEVERE, "Authentication error or add a new user", ex);
                }

                if (user != null) {
                    subscribe(user.getLogin(), socket);
                    logger.info("Подключился пользователь " + user.getLogin());
                } else {
                    logger.warning("Пользователю не удалось авторизоваться!");
                    socket.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, "Не удалось запустить сервер!!!", ex);
        }
    }

    private void sendUserConnectedMessage(String login) throws IOException {
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            if (!clientHandler.getLogin().equals(login)) {
                System.out.printf("Sending connect notification to %s about %s%n", clientHandler.getLogin(), login);
                logger.fine("Попытка подключения пользователя " + login);
                clientHandler.sendConnectedMessage(login);
            }
        }
    }

    private void sendUserDisconnectedMessage(String login) throws IOException {
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            if (!clientHandler.getLogin().equals(login)) {
                System.out.printf("Sending disconnect notification to %s about %s%n", clientHandler.getLogin(), login);
                logger.fine("Попытка отключения пользователя " + login);
                clientHandler.sendDisconnectedMessage(login);
            }
        }
    }

    public void sendMessage(TextMessage msg) throws IOException {
        ClientHandler userToClientHandler = clientHandlerMap.get(msg.getUserTo());
        if (userToClientHandler != null) {
            logger.fine("Пользователь *" + msg.getUserFrom() + " посылает сообщение : "
                    + msg.getText() + " пользователю *" + msg.getUserTo());
            userToClientHandler.sendMessage(msg.getUserFrom(), msg.getText());
        } else {
            System.out.printf("User %s not connected%n", msg.getUserTo());
            logger.fine("Пользователь *" + msg.getUserFrom() + " посылает сообщение : "
                    + msg.getText() + " пользователю *" + msg.getUserTo() + ", который не подключен к чату.");
        }
    }

    public Set<String> getUserList() {
        return Collections.unmodifiableSet(clientHandlerMap.keySet());
    }

    public boolean subscribe(String login, Socket socket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(login, socket, limitedExecutorService, this);
        clientHandlerMap.put(login, clientHandler);
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
            logger.log(Level.SEVERE, "Ошибка при отсоединении пользователя " + login, e);
        }
    }
}
