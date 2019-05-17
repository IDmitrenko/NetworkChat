package ru.geekbrains.client.history;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestPath {
    public static void main(String[] args) throws IOException {

        String pathHistoryMessage = "E:\\GeekBrains\\NetworkChat\\File";
        Path path = Paths.get(pathHistoryMessage);
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
//            File file = path.toFile();
//            file.mkdirs();
            Files.createDirectories(path);
        }

        String login = "Dias";
        String nameFileMessage = "\\history_" + login + ".txt";
        Path pathF = Paths.get(pathHistoryMessage + nameFileMessage);
        if (!Files.exists(pathF, LinkOption.NOFOLLOW_LINKS)) {
            Files.createFile(pathF);
        }


    }
}
