package Database;

import OrderManager.Order;

import java.sql.*;

public class DatabaseController {

    Connection connection;
    String connectionString;

    public DatabaseController(String connection, String url, String username, String password) throws SQLException, ClassNotFoundException {
        connectToDateBase(connection,url,username,password);
    }

    public void connectToDateBase(String connection, String url, String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName(connection);

        Connection conn = DriverManager.getConnection(url,username,password);
    }

    public void createDatabase() throws SQLException {
        String createStatement = "create table orders(order_id int not null, client_order_id int not null, client_id int not null, ric varchar(30) not null, initial_market_price double not null,primary key (order_id));";
        PreparedStatement statementObj = connection.prepareStatement(createStatement);
        statementObj.executeUpdate();
        statementObj.close();
    }

    public void addOrderToDatabase(Order o) throws SQLException {
        String statement = "insert into orders values(?,?,?,?,?)";
        PreparedStatement addStatement = connection.prepareStatement(statement);
        addStatement.setInt(1,o.getOrderId());
        addStatement.setLong(2,o.getClientOrderID());
        addStatement.setLong(3,o.getClientId());
        addStatement.setString(4,o.getInstrument().toString());
        addStatement.setDouble(5,o.getInitialMarketPrice());

        int addedStatement = addStatement.executeUpdate();
        if(addedStatement > 0){
            System.out.println("Order inserted");
        }else
            System.out.println("Record failed to add");

        addStatement.close();
    }
}
