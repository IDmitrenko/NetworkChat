package ru.geekbrains.server.LogHandler;

import java.util.logging.*;
import java.sql.*;

/**
 * JDBC Logging
 */
public class JDBCLogHandler extends Handler {

    private final String tableName = "log";

    // объект подключения
    Connection connection;

    // SQL для записи в таблицу лога.
    protected final  String insertSQL=
            "insert into " + tableName +
                    " (level,logger,message,sequence,"
                    +"sourceClass,sourceMethod,threadID,timeEntered)"
                    +"values(?,?,?,?,?,?,?,?);";

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
}
