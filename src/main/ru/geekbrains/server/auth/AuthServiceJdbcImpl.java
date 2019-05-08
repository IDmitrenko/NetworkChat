package ru.geekbrains.server.auth;

import ru.geekbrains.server.User;
import ru.geekbrains.server.persistance.UserRepository;

public class AuthServiceJdbcImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceJdbcImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean authUser(User user) {
        // Авторизовать пользователя используя userRepository
        User userFromDB = userRepository.findByLogin(user.getLogin());
        return userFromDB != null && userFromDB.getPassword().equals(user.getPassword());
    }

    @Override
    public boolean createUser(User user) {
        // Добавить нового пользователя в БД
        // Проверяем может он уже есть
        User userFromDB = userRepository.findByLogin(user.getLogin());
        if (userFromDB != null) {
            // TODO ?? сообщение, что такой пользователь уже есть
            return false;
        }
        // Добавляем нового пользователя
        if (userRepository.insert(user)){

            return true;
        }

        // Добавить не удалось
        return false;
    }
}
