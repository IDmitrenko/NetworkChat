package ru.geekbrains.server.persistance;

import ru.geekbrains.server.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static ru.geekbrains.server.ChatServer.logger;

public class UserRepository {

    private final Connection conn;
    private final String tableName = "users";

    public UserRepository(Connection conn) throws SQLException {
        this.conn = conn;
        // Создать таблицу пользователей, если она еще не создана
        createTableUsers(conn);
    }

    public boolean insert(User user) {
        // Добавить нового пользователя в БД
        String SQL = "INSERT INTO " + tableName +
                " (login , password) " +
                "VALUES (?, ?);";

        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL)) {
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.execute();
        } catch (SQLException ex) {
            printSQLException(ex);
            return false;
        }
        return true;
    }

    public User findByLogin(String login) {
        // Найти пользователя в БД по логину
        User user = new User(-1, "", "");
        try (PreparedStatement stmt = conn.prepareStatement("select id, login, password from users where login=?;");
        ) {
            stmt.setString(1, login);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                user = new User(resultSet.getInt("id"),
                        resultSet.getString("login"),
                        resultSet.getString("password"));
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return user;
    }

    public List<User> getAllUsers() {
        // Извлечь из БД полный список пользователей
        List<User> users = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery("select id, login, password from users;")) {
            while (resultSet.next()) {
                User user = new User(resultSet.getInt("id"),
                        resultSet.getString("login"),
                        resultSet.getString("password"));
                users.add(user);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return users;
    }

    private void createTableUsers(Connection conn) {
        // Проверить существует ли таблица users
//        DatabaseMetaData md = conn.getMetaData();
//        ResultSet rs = md.getTables("network_chat", "network_chat", tableName, null);
//        if (!rs.next()) {
            // Если нет, то создать ее
            try (Statement statement = conn.createStatement()) {
                String SQL = "CREATE TABLE IF NOT EXISTS " + tableName +
                        " (id INT NOT NULL AUTO_INCREMENT, " +
                        "login VARCHAR(25) NOT NULL , " +
                        "password VARCHAR(25) NOT NULL , " +
                        "PRIMARY KEY (id), " +
                        "UNIQUE INDEX uq_login(login));";

                statement.executeUpdate(SQL);
            } catch (SQLException ex) {
                printSQLException(ex);
            }
        }
//    }

    private static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(((SQLException) e).getSQLState())) {

                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " +
                            ((SQLException) e).getSQLState());

                    System.err.println("Error Code: " +
                            ((SQLException) e).getErrorCode());

                    System.err.println("Message: " + e.getMessage());

                    logger.severe("SQLState: " +
                            ((SQLException) e).getSQLState() +
                                "%n Error Code: " +
                            ((SQLException) e).getErrorCode() +
                                "%n Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        logger.warning("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    private static boolean ignoreSQLException(String sqlState) {

        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            logger.warning("The SQL state is not defined!");
            return false;
        }

        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;

        // 42Y55: Table already exists in schema
        return sqlState.equalsIgnoreCase("42Y55");

    }

}
