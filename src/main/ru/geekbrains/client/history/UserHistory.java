package ru.geekbrains.client.history;

import ru.geekbrains.client.TextMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import static ru.geekbrains.client.swing.TextMessageCellRenderer.timeFormatter;

public class UserHistory {

    private final String pathHistoryMessage;

    public UserHistory(String pathHistoryMessage) {
        this.pathHistoryMessage = pathHistoryMessage;
    }

    public void saveHistory(TextMessage msg, String login) throws IOException {

        Path path = createFileObject(login);
        String nameFileMessage = "\\history_" + login + ".txt";
        //открываем поток для записи в файл текстовой информации в режиме добавления
        File printTextFile = new File(pathHistoryMessage + nameFileMessage);
        try (PrintWriter wr = new PrintWriter(new BufferedWriter(new FileWriter(printTextFile, true)))) {
            wr.println(msg.getCreated().format(timeFormatter) + " " + msg.getUserFrom());
            wr.println(msg.getText());
            wr.flush();
        }
    }

    private Path createFileObject(String login) throws IOException {
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
        return pathF;
    }

    public String listHistory(String login) throws IOException {

        Path path = createFileObject(login);
        File file = path.toFile();
        int readLines = 100 * 2;

        return readFromLast(file, readLines);
        // TODO замерить время работы
//        return lastNLines(file, readLines);
    }

    private String readFromLast(File file, int lines) throws UnsupportedEncodingException {
        LinkedList<Byte> bytes = new LinkedList<>();
        int readLines = 0;
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long filelength = file.length() - 1;
            // установим показатель на последний байт
            randomAccessFile.seek(filelength);
            for (long pointer = filelength; pointer >= 0; pointer--) {
                randomAccessFile.seek(pointer);
                byte rc;
                // читаем текущий символ
                rc = (byte) randomAccessFile.read();
                // ищем перевод строки
                if ((char) rc == '\n') {
                    readLines++;
                    if (readLines == lines)
                        break;
                }
                bytes.addFirst(rc);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        byte[] result = new byte[bytes.size()];
        int i = 0;
        for (Byte aByte : bytes) {
            result[i++] = aByte;
        }
        return new String(result, "UTF-8");
    }

    private String lastNLines(File file, int lines) {
        StringBuilder message = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            int numberLine = 0;
            while (br.ready()) {
                message.append(br.readLine());
                numberLine++;
            }
            message = new StringBuilder();
            // результат считываем с определенной строки
            StringBuilder temp = new StringBuilder();
            br.close();
            br = new BufferedReader(new FileReader(file));
            while (br.ready()) {
                if (numberLine <= lines) {
                    message.append(br.readLine() + '\n');
                } else {
                    temp.append(br.readLine());
                    numberLine--;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return message.toString();
    }

}
