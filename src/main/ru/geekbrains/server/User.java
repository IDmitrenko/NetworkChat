package ru.geekbrains.server;

import ru.geekbrains.server.orm.*;

@Table(tableName = "USERS")
public class User {

    @PrimaryKey
    @AutoIncrement
    @Field(name = "id")
    private int id;

    @Unique
    @Index
    @NotNull
    @Field(name = "login", length = 25)
    private String login;

    @NotNull
    @Field(name = "password", length = 25)
    private String password;

    public User(int id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
