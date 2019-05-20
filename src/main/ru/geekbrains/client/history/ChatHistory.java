package ru.geekbrains.client.history;

import ru.geekbrains.client.TextMessage;

import java.util.List;

public interface ChatHistory {

    void saveHistory(TextMessage msg);

    List<TextMessage> listHistory(int count);

    void flush();
}
