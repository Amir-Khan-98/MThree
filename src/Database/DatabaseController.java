package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by delegate on 15/01/2020.
 */
public class DatabaseController {

    private static Connection connection;

    public DatabaseController(String connectionString, String url, String username, String password) throws SQLException, ClassNotFoundException {
        connectToDatebase(connectionString,url,username,password);
    }

    public void connectToDatebase(String connectionString, String url, String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName(connectionString);

        connection = DriverManager.getConnection(url,username,password);
    }

    public static Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
