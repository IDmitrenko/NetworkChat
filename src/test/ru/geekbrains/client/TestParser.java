package ru.geekbrains.client;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ru.geekbrains.client.MessagePatterns.*;

/**
 * Unit тесты для различных методов сетевого чата
 * Прежде чем запускать, необходимо отметить папку test как Test Source Root
 */
public class TestParser {

    @Test
    public void testParserRegx() {
        TextMessage textMessage = parseTextMessageRegx("/w userFrom Example of message", "userTo");

        assertNotNull(textMessage);
        assertEquals("userFrom", textMessage.getUserFrom());
        assertEquals("Example of message", textMessage.getText());
        assertEquals("userTo", textMessage.getUserTo());
    }

    @Test
    public void testParser() {
        TextMessage textMessage = parseTextMessage("/w userFrom Example of message", "userTo");

        assertNotNull(textMessage);
        assertEquals("userFrom", textMessage.getUserFrom());
        assertEquals("Example of message", textMessage.getText());
        assertEquals("userTo", textMessage.getUserTo());
    }

    @Test
    public void testParseUserList() {
        Set<String> userList = parseUserList("/user_list ivan petr");

        assertNotNull(userList);
        assertTrue(userList.contains("ivan"));
        assertTrue(userList.contains("petr"));
    }

    @Test
    public void testParseConnectedMessage() {
        String user = parseConnectedMessage("/connected ivan");

        assertNotNull(user);
        assertEquals("ivan", user);
    }

    @Test
    public void testParseDisconnectedMessage() {
        String user = parseDisconnectedMessage("/disconnect petr");

        assertNotNull(user);
        assertEquals("petr", user);
    }

}
