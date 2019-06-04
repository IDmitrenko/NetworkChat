package ru.geekbrains.server.auth;

import ru.geekbrains.server.User;
import ru.geekbrains.server.persistance.UserRepository;

import java.sql.SQLException;
import java.util.logging.Logger;

public class AuthServiceJdbcImpl implements AuthService {

    private static final Logger logger = Logger.getLogger(AuthServiceJdbcImpl.class.getName());
    private final UserRepository userRepository;

    public AuthServiceJdbcImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean authUser(User user) {
        // Авторизовать пользователя используя userRepository
        User userFromDB = userRepository.findByLogin(user.getLogin());
        return userFromDB.getId() > 0 && userFromDB.getPassword().equals(user.getPassword());
    }

    @Override
    public boolean createUser(User user) {
        // Добавить нового пользователя в БД
        // Проверяем может он уже есть
        User userFromDB = userRepository.findByLogin(user.getLogin());
        if (userFromDB.getId() > 0) {
            return false;
        }
        // Добавляем нового пользователя
        logger.info("Добавили нового пользователя " + user.getLogin() + " в БД.");
        try {
            return userRepository.insert(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
