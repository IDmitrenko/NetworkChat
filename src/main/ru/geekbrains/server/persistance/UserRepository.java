package ru.geekbrains.server.persistance;

import ru.geekbrains.server.User;
import ru.geekbrains.server.orm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UserRepository<T> {

    private final Connection conn;
    private Class<T> clazz;
    private final String tableName = "users";
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

    public UserRepository(Connection conn, Class<T> clazz) throws SQLException {
        this.conn = conn;
        this.clazz = clazz;
        // Создать таблицу, если она еще не создана
        createTableIfNotExists();
    }

/*
    public boolean insert(User user) {
        // Добавить нового пользователя в БД
        String SQL = "INSERT INTO " + tableName +
                " (login , password) " +
                "VALUES (?, ?);";

        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL)) {
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.execute();
        } catch (SQLException ex) {
            printSQLException(ex);
            return false;
        }
        return true;
    }
*/
public boolean insert(T obj) throws SQLException {

    boolean rez = false;
    if (!clazz.isAnnotationPresent(Table.class)) {
        throw new IllegalStateException("No Table annotation");
    }
    String tableName = clazz.getAnnotation(Table.class).tableName();
    tableName = tableName.isEmpty() ? clazz.getSimpleName() : tableName;

    StringBuilder sb = new StringBuilder();
    sb.append("insert into " + tableName + "(");

    Map<String, Object> nameAndValue = new LinkedHashMap<>();

    sb = buildQueryAddRecord(sb, nameAndValue, obj);

    try (PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
        int i = 1;
        for (Map.Entry<String, Object> keyVal : nameAndValue.entrySet()) {
            if (keyVal.getValue() != null) {
                if (keyVal.getValue() instanceof Integer) {
                    stmt.setInt(i, (Integer) keyVal.getValue());
                } else if (keyVal.getValue() instanceof String) {
                    stmt.setString(i, (String) keyVal.getValue());
                } else if (keyVal.getValue() instanceof Boolean) {
                    stmt.setBoolean(i, (Boolean) keyVal.getValue());
                } else if (keyVal.getValue() instanceof Byte) {
                    stmt.setByte(i, (Byte) keyVal.getValue());
                } else if (keyVal.getValue() instanceof Float) {
                    stmt.setFloat(i, (Float) keyVal.getValue());
                } else if (keyVal.getValue() instanceof Double) {
                    stmt.setDouble(i, (Double) keyVal.getValue());
                } else if (keyVal.getValue() instanceof Long) {
                    stmt.setLong(i, (Long) keyVal.getValue());
                } else if (keyVal.getValue() instanceof Short) {
                    stmt.setShort(i, (Short) keyVal.getValue());
                } else if (keyVal.getValue() instanceof Blob) {
                    stmt.setBlob(i, (Blob) keyVal.getValue());
                }
                i++;
            }
        }
        stmt.execute();
        rez = true;
    }
    return rez;
}

    private StringBuilder buildQueryAddRecord(StringBuilder sb, Map nameAndValue, T obj) {
        StringBuilder vl = new StringBuilder();
        vl.append(" values (");

        for (Field fld : clazz.getDeclaredFields()) {
            if (!fld.isAnnotationPresent(ru.geekbrains.server.orm.Field.class) ||
                    fld.isAnnotationPresent(PrimaryKey.class) ||
                    fld.isAnnotationPresent(AutoIncrement.class))
                continue;

            ru.geekbrains.server.orm.Field fldAnnotation = fld.getAnnotation(ru.geekbrains.server.orm.Field.class);
            String fieldName = fldAnnotation.name().isEmpty() ? fld.getName() : fldAnnotation.name();
            try {
                Object getValue = clazz.getMethod("get" + fieldName.substring(0, 1)
                        .toUpperCase() + fieldName.substring(1), null)
                        .invoke(obj);
                if (getValue != null) {
                    nameAndValue.put(fieldName, getValue);
                    sb.append(fieldName + ",");
                    vl.append("?,");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        if (vl.length() > 0) {
            vl.deleteCharAt(vl.length() - 1);
            vl.append(")");
            sb.append(vl);
        }
        sb.append(";");

        return sb;
    }

    public User findByLogin(String login) {
        // Найти пользователя в БД по логину
        User user = new User(-1, "", "");
        try (PreparedStatement stmt = conn.prepareStatement("select id, login, password from users where login=?;");
        ) {
            stmt.setString(1, login);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                user = new User(resultSet.getInt("id"),
                        resultSet.getString("login"),
                        resultSet.getString("password"));
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return user;
    }

/*
    public List<User> getAllUsers() {
        // Извлечь из БД полный список пользователей
        List<User> users = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery("select id, login, password from users;")) {
            while (resultSet.next()) {
                User user = new User(resultSet.getInt("id"),
                        resultSet.getString("login"),
                        resultSet.getString("password"));
                users.add(user);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return users;
    }
*/

    public List<T> getAll() throws SQLException {
        List<T> res = new ArrayList<>();
        String stringSQL;

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalStateException("No Table annotation");
        }

        stringSQL = buildListFieldsForQuery();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(stringSQL);
            T obj = null;
            Object value = null;
            int i;

            while (rs.next()) {
                Constructor<T> constructor = null;
                try {
                    constructor = clazz.getConstructor();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                try {
                    obj = constructor.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                i = 1;
                for (Field fld : clazz.getDeclaredFields()) {
                    if (!fld.isAnnotationPresent(ru.geekbrains.server.orm.Field.class) ||
                            (fld.isAnnotationPresent(PrimaryKey.class) &&
                                    fld.isAnnotationPresent(AutoIncrement.class)))
                        continue;

                    ru.geekbrains.server.orm.Field fldAnnotation = fld.getAnnotation(ru.geekbrains.server.orm.Field.class);
                    String fieldName = fldAnnotation.name().isEmpty() ? fld.getName() : fldAnnotation.name();
                    try {
                        value = rsGetField(rs, fld, i);
                        if (value != null) {
                            Object setValue = clazz.getMethod("set" + fieldName.substring(0, 1)
                                    .toUpperCase() + fieldName.substring(1), value.getClass())
                                    .invoke(obj, value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    i++;

                }
                res.add(obj);

            }
        }

        return res;
    }

    private Object rsGetField(ResultSet rs, Field fld, int i) throws SQLException {

        Class<?> type = fld.getType();
        if (type == int.class) {
            return rs.getInt(i);
        } else if (type == String.class) {
            return rs.getString(i);
        } else if (type == boolean.class) {
            return rs.getBoolean(i);
        } else if (type == byte.class) {
            return rs.getByte(i);
        } else if (type == double.class) {
            return rs.getDouble(i);
        } else if (type == float.class) {
            return rs.getFloat(i);
        } else if (type == long.class) {
            return rs.getLong(i);
        } else if (type == short.class) {
            return rs.getShort(i);
        } else if (type == java.util.Date.class) {
            return rs.getDate(i);
        } else if (type == java.io.Serializable.class) {
            return rs.getBlob(i);
        }

        return null;
    }

    private String buildListFieldsForQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        String tableName = clazz.getAnnotation(Table.class).tableName();
        tableName = tableName.isEmpty() ? clazz.getSimpleName() : tableName;

        for (Field fld : clazz.getDeclaredFields()) {
            if (!fld.isAnnotationPresent(ru.geekbrains.server.orm.Field.class)) {
                continue;
            }
            ru.geekbrains.server.orm.Field fldAnnotation = fld.getAnnotation(ru.geekbrains.server.orm.Field.class);
            String fieldName = fldAnnotation.name().isEmpty() ? fld.getName() : fldAnnotation.name();

            boolean isPrimaryKey = fld.isAnnotationPresent(PrimaryKey.class);
            boolean isAutoIncrement = fld.isAnnotationPresent(AutoIncrement.class);

            if (!(isAutoIncrement && isPrimaryKey)) {
                sb.append(fieldName);
                sb.append(",");
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(" from ");
        sb.append(tableName);
        sb.append(";");
        return sb.toString();

    }

/*
    private void createTableUsers(Connection conn) {
        // Проверить существует ли таблица users
//        DatabaseMetaData md = conn.getMetaData();
//        ResultSet rs = md.getTables("network_chat", "network_chat", tableName, null);
//        if (!rs.next()) {
            // Если нет, то создать ее
            try (Statement statement = conn.createStatement()) {
                String SQL = "CREATE TABLE IF NOT EXISTS " + tableName +
                        " (id INT NOT NULL AUTO_INCREMENT, " +
                        "login VARCHAR(25) NOT NULL , " +
                        "password VARCHAR(25) NOT NULL , " +
                        "PRIMARY KEY (id), " +
                        "UNIQUE INDEX uq_login(login));";

                statement.executeUpdate(SQL);
            } catch (SQLException ex) {
                printSQLException(ex);
            }
        }
//    }
*/

    public void createTableIfNotExists() throws SQLException {
        String stringSQL = buildCreateTableStatement();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(stringSQL);
        }
    }

    public String buildCreateTableStatement() {
        StringBuilder sb = new StringBuilder();
        StringBuilder ind = new StringBuilder();
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalStateException("No Table annotation");
        }
        String tableName = clazz.getAnnotation(Table.class).tableName();
        tableName = tableName.isEmpty() ? clazz.getSimpleName() : tableName;
        sb.append("create table if not exists " + tableName + "(");

        for (Field fld : clazz.getDeclaredFields()) {
            if (!fld.isAnnotationPresent(ru.geekbrains.server.orm.Field.class)) {
                continue;
            }
            ru.geekbrains.server.orm.Field fldAnnotation = fld.getAnnotation(ru.geekbrains.server.orm.Field.class);
            String fieldName = fldAnnotation.name().isEmpty() ? fld.getName() : fldAnnotation.name();
            int fieldLength = fldAnnotation.length();
            String fieldType = null;
            Class<?> type = fld.getType();
            if (type == int.class) {
                fieldType = "int";
            } else if (type == String.class) {
                if (fieldLength == 0) {
                    fieldType = "varchar(25)";
                } else {
                    fieldType = "varchar(" + fieldLength + ")";
                }
            } else if (type == boolean.class) {
                fieldType = "bit";
            } else if (type == byte.class) {
                fieldType = "tinyint";
            } else if (type == char.class) {
                fieldType = "char";
            } else if (type == double.class) {
                fieldType = "double";
            } else if (type == float.class) {
                fieldType = "real";
            } else if (type == long.class) {
                fieldType = "bigint";
            } else if (type == short.class) {
                fieldType = "smallint";
            } else if (type == java.util.Date.class) {
                fieldType = "datetime";
            } else if (type == java.io.Serializable.class) {
                fieldType = "blob";
            }

            boolean isPrimaryKey = fld.isAnnotationPresent(PrimaryKey.class);
            boolean isNotNull = fld.isAnnotationPresent(NotNull.class);
            boolean isUnique = fld.isAnnotationPresent(Unique.class);
            boolean isIndex = fld.isAnnotationPresent(Index.class);
            boolean isAutoIncrement = fld.isAnnotationPresent(AutoIncrement.class);

            sb.append(fieldName + " " + fieldType +
                    (isPrimaryKey ? " primary key" : "") +
                    (isNotNull ? " not null" : "") +
                    (isUnique ? " unique" : "") +
                    (isAutoIncrement ? " auto_increment" : "") +
                    ",");

            if (isIndex)
                ind.append(" index uq_" + fieldName + "(" + fieldName + "),");
        }

        if (ind.length() > 0)
            sb.append(ind);

        sb.deleteCharAt(sb.length() - 1);
        sb.append(");");
        return sb.toString();
    }

    public static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(((SQLException) e).getSQLState())) {

                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " +
                            ((SQLException) e).getSQLState());

                    System.err.println("Error Code: " +
                            ((SQLException) e).getErrorCode());

                    System.err.println("Message: " + e.getMessage());

                    logger.severe("SQLState: " +
                            ((SQLException) e).getSQLState() +
                                "%n Error Code: " +
                            ((SQLException) e).getErrorCode() +
                                "%n Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        logger.warning("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    private static boolean ignoreSQLException(String sqlState) {

        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            logger.warning("The SQL state is not defined!");
            return false;
        }

        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;

        // 42Y55: Table already exists in schema
        return sqlState.equalsIgnoreCase("42Y55");

    }

}
