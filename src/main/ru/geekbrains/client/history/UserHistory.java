package ru.geekbrains.client.history;

import ru.geekbrains.client.TextMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.geekbrains.client.swing.TextMessageCellRenderer.timeFormatter;

public class UserHistory implements ChatHistory {

    private static final String MSG_PATTERN = "%s\t%s\t%s\t%s";

    private final String pathHistoryMessage;
    private final String login;

    private final Path path;
    private final PrintWriter wr;
    private final String nameFileMessage;
    private final File printTextFile;


    public UserHistory(String pathHistoryMessage, String login) throws IOException {
        this.pathHistoryMessage = pathHistoryMessage;
        this.login = login;
        nameFileMessage = "\\history_" + login + ".txt";
        path = createFileObject(login);
        //открываем поток для записи в файл текстовой информации в режиме добавления
        printTextFile = new File(pathHistoryMessage + nameFileMessage);
        wr = new PrintWriter(new BufferedOutputStream(new FileOutputStream(printTextFile, true)));
//        wr = new PrintWriter(new BufferedWriter(new FileWriter(printTextFile, true)));
    }

    @Override
    public synchronized void saveHistory(TextMessage message) {
        String msg = String.format(MSG_PATTERN, message.getCreated().format(timeFormatter),
                message.getUserFrom(), message.getUserTo(), message.getText());
        wr.println(msg);
    }

    private Path createFileObject(String login) throws IOException {
        //Пример строки создания объекта Path пути для запуска в Windows
        //Path path = Paths.get("E:\\GeekBrains\\NetworkChat\\File");
        Path pathD = Paths.get(pathHistoryMessage);
        if (!Files.exists(pathD, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(pathD);
        }

        Path pathF = Paths.get(pathHistoryMessage + nameFileMessage);
        if (!Files.exists(pathF, LinkOption.NOFOLLOW_LINKS)) {
            Files.createFile(pathF);
        }
        return pathF;
    }

    @Override
    public List<TextMessage> listHistory(int count) {
        List<String> msgs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(printTextFile))) {
            while (reader.ready()) {
                msgs.add(reader.readLine());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        List<TextMessage> textMessageList = new ArrayList<>();
        if (msgs.size() > count) {
            msgs = msgs.subList(msgs.size() - count, msgs.size());
        }
        for (String str : msgs) {
            textMessageList.add(parseMsg(str));
        }

        return textMessageList;
    }

    @Override
    public void flush() {
        wr.flush();
    }

    private TextMessage parseMsg(String str) {
        String[] part = str.split("\t", 4);
        return new TextMessage(part[1], part[2], part[3], LocalDateTime.parse(part[0], timeFormatter));
    }

/*
        public List<TextMessage> listHistory(int count) throws IOException {

        List<TextMessage> textMessageList = new ArrayList<>();
        Path path = createFileObject(login);
        File file = path.toFile();
        int readLines = count * 2;

        List<String> messageList = lastNLines(file, readLines);
        int i = 0;
        String part1 = null;
        String part2 = null;
        for (String part : messageList) {
            i++;
            if (i%2 == 0) {
                part2 = part;
            } else {
                part1 = part;
            }
            if (part1 != null && part2 != null) {
                TextMessage textMessage = parseListMessageRegx(part1, part2, login);
                textMessageList.add(textMessage);
            }
        }
        return textMessageList;
    }

    private List<String> lastNLines(File file, int lines) {
        StringBuilder message = new StringBuilder();
        List<String> messageList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            int numberLine = 0;
            while (br.ready()) {
                message.append(br.readLine());
                numberLine++;
            }
            br.close();
            br = new BufferedReader(new FileReader(file));
            message = new StringBuilder();
            // результат считываем с определенной строки
            while (br.ready()) {
                if (numberLine <= lines) {
                    messageList.add(br.readLine());
                } else {
                    message.append(br.readLine());
                    numberLine--;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return messageList;
    }
*/

    /*
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

    */

}
