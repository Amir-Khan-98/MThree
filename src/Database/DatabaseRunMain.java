package Database;

import java.sql.*;

public class DatabaseRunMain {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DatabaseController dbc = new DatabaseController("com.mysql.jdbc.Driver","jdbc:mysql://localhost/marketdatabase","root","");
    }

}
