package Database;

import OrderManager.Order;
import java.sql.*;

public class DatabaseController {

    private static Connection connection;

    public DatabaseController(String connectionString, String url, String username, String password) throws SQLException, ClassNotFoundException {
        connectToDatebase(connectionString,url,username,password);
    }

    public void connectToDatebase(String connectionString, String url, String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName(connectionString);

        connection = DriverManager.getConnection(url,username,password);
    }

    /**
     * Creates a table of orders
     *
     * @throws SQLException
     */
    public void createOrderTable() throws SQLException {
        String createStatement = "create table orders(order_id int not null, client_order_id int not null, client_id int not null, ric varchar(30) not null, initial_market_price double not null,primary key (order_id));";
        PreparedStatement statementObj = connection.prepareStatement(createStatement);
        statementObj.executeUpdate();
        statementObj.close();
    }

    public static void addOrderToTable(Order o) throws SQLException {
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

    public static void removeOrderFromTable(Order o) throws SQLException {
        String statementString = "delete from orders where order_id = ?";
        PreparedStatement deleteStatement = connection.prepareStatement(statementString);
        deleteStatement.setInt(1,o.getOrderId());
        deleteStatement.executeUpdate();
    }


    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
