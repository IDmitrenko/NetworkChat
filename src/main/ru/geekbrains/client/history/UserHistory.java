package ru.geekbrains.client.history;

import ru.geekbrains.client.TextMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.geekbrains.client.swing.TextMessageCellRenderer.timeFormatter;

public class UserHistory {

    private final String pathHistoryMessage;

    public UserHistory(String pathHistoryMessage) {
        this.pathHistoryMessage = pathHistoryMessage;
    }

    public void saveHistory(TextMessage msg, String login) throws IOException {

//Пример строки создания объекта Path пути для запуска в Windows
        //Path path = Paths.get("E:\\GeekBrains\\NetworkChat\\File");
        Path pathD = Paths.get(pathHistoryMessage);
        if (!Files.exists(pathD, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(pathD);
        }

        String nameFileMessage = "\\history_" + login + ".txt";
        Path pathF = Paths.get(pathHistoryMessage + nameFileMessage);
        if (!Files.exists(pathF, LinkOption.NOFOLLOW_LINKS)) {
            Files.createFile(pathF);
        }

        //открываем поток для записи в файл текстовой информации в режиме добавления
        File printTextFile = new File(pathHistoryMessage + nameFileMessage);
        try (PrintWriter wr = new PrintWriter(new BufferedWriter(new FileWriter(printTextFile, true)))) {
            wr.println(msg.getCreated().format(timeFormatter) + " " + msg.getUserFrom());
            wr.println(msg.getText());
            wr.flush();
        }

    }
}
