package Database;

import OrderManager.Order;

import javax.xml.transform.Result;
import java.sql.*;

public class OrderController {
    /**
     * Creates a table of orders
     *
     * @throws SQLException
     */
    public void createOrderTable(Connection connection) throws SQLException {
        String createStatement = "create table orders(order_id int not null, client_order_id int not null, client_id int not null, ric varchar(30) not null, initial_market_price double not null,primary key (order_id));";
        PreparedStatement statementObj = connection.prepareStatement(createStatement);
        statementObj.executeUpdate();
        statementObj.close();
    }

    public static void addOrderToTable(Connection connection, Order o) throws SQLException {
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

    public static ResultSet selectAll(Connection connection) throws SQLException {
        String selectStatement = "select * from orders;";
        PreparedStatement statement = connection.prepareStatement(selectStatement);
        ResultSet resultSet = statement.executeQuery();
        return resultSet;
    }

    public static void printResultSet(ResultSet rs) throws SQLException {
        while(rs.next()){
            System.out.println("-------------------------------");
            System.out.println("Order ID: "+rs.getString("order_id"));
            System.out.println("Client Order ID: "+rs.getString("client_order_id"));
            System.out.println("Client ID: "+rs.getString("client_id"));
            System.out.println("RIC: "+rs.getString("ric"));
            System.out.println("Initial Market Price: "+rs.getString("order_id"));
        }

    }

    public static void removeOrderFromTable(Connection connection, Order o) throws SQLException {
        String statementString = "delete from orders where order_id = ?";
        PreparedStatement deleteStatement = connection.prepareStatement(statementString);
        deleteStatement.setInt(1,o.getOrderId());
        deleteStatement.executeUpdate();
    }

}
