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
}
