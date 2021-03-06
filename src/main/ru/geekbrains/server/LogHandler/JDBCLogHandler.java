package ru.geekbrains.server.LogHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static ru.geekbrains.server.persistance.UserRepository.printSQLException;

/**
 * JDBC Logging
 */
public class JDBCLogHandler extends Handler {

    private final String tableName = "log";
    private final StringWriter sw = new StringWriter();
    private static final Logger logger = Logger.getLogger(JDBCLogHandler.class.getName());

    // объект подключения
    Connection connection;

    // SQL для записи в таблицу лога.
    protected final  String insertSQL=
            "insert into " + tableName +
                    " (level,logger,message,sequence," +
                    "sourceClass,sourceMethod,threadID,timeEntered,trace)" +
                    "values(?,?,?,?,?,?,?,?,?);";

    // SQL для очистки таблицы журнала.
    protected final String clearSQL=
            "delete from " + tableName + ";";

    // PreparedStatement - обьект для хранения оператора insert
    protected PreparedStatement prepInsert;

    // PreparedStatement - обьект для хранения оператора clear
    protected PreparedStatement prepClear;


    public JDBCLogHandler(Connection connection)
    {
        try {
            this.connection = connection;
            createTableLog(connection);

            prepInsert = connection.prepareStatement(insertSQL);
            prepClear = connection.prepareStatement(clearSQL);

        } catch ( SQLException e ) {
            System.err.println("Error on open: " + e);
        }
    }

    // усечение записываемой информации до ширины поля
    static public String truncate(String str,int length)
    {
        if ( str.length()<length )
            return str;
        return( str.substring(0,length) );
    }

    // обработка записей logger и запись их в БД
    @Override
    public void publish(LogRecord record)
    {
        // проверяем есть ли текущий фильтр
        // если есть проверяем подходит ли для него наша запись
        if ( getFilter()!=null ) {
            if ( !getFilter().isLoggable(record) )
                return;
        }

        // сохраняем запись журнала в таблице
        try {
            prepInsert.setInt(1,record.getLevel().intValue());
            prepInsert.setString(2,truncate(record.getLoggerName(),64));
            prepInsert.setString(3,truncate(record.getMessage(),255));
            prepInsert.setLong(4,record.getSequenceNumber());
            prepInsert.setString(5,truncate
                    (record.getSourceClassName(),64));
            prepInsert.setString(6,truncate
                    (record.getSourceMethodName(),32));
            prepInsert.setInt(7,record.getThreadID());
            prepInsert.setTimestamp(8,
                    new Timestamp
                            (System.currentTimeMillis()) );
            java.sql.Blob blob = null;
            if (record.getThrown() != null) {
                record.getThrown().printStackTrace(new PrintWriter(sw));
                blob = connection.createBlob();
                blob.setBytes(1, sw.toString().getBytes());
            }
            prepInsert.setBlob(9, blob);

            prepInsert.execute();
        } catch ( SQLException e ) {
            System.err.println("Error on open: " + e);
        }

    }

    // для закрытия обработчика
    @Override
    public void close()
    {
        try {
            if ( connection!=null )
                connection.close();
        } catch ( SQLException e ) {
            System.err.println("Error on close: " + e);
        }
    }

    // очистка записей журнала в БД
    public void clear()
    {
        try {
            prepClear.executeUpdate();
        } catch ( SQLException e ) {
            System.err.println("Error on clear: " + e);
        }
    }

    // для БД не нужен
    // надо переопределить, так как он есть в родительском абстрактном классе Handler
    @Override
    public void flush()
    {
    }

    private void createTableLog(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            String SQL = "CREATE TABLE IF NOT EXISTS " + tableName +
                    " (level integer NOT NULL, " +
                    "logger varchar(64) NOT NULL, " +
                    "message varchar(255) NOT NULL, " +
                    "sequence integer NOT NULL, " +
                    "sourceClass varchar(64) NOT NULL, " +
                    "sourceMethod varchar(32) NOT NULL, " +
                    "threadID integer NOT NULL, " +
                    "timeEntered datetime NOT NULL," +
                    "trace blob NULL);";
            statement.executeUpdate(SQL);
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

}
