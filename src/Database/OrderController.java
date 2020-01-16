package Database;

import OrderManager.Order;

import javax.xml.transform.Result;
import java.sql.*;

public class OrderController {
    private static Connection connection;

    public OrderController(Connection connection){
        this.connection = connection;
    }

    /**
     * Creates a table of orders
     *
     * @throws SQLException
     */
    public static void createOrderTable()  {
        try{
            String createStatement = "create table orders(order_id int not null,ric varchar(30) not null,unit_price double not null,client_id int not null, size int not null,primary key (order_id),foreign key (client_id) references clients(client_id));";
            PreparedStatement statementObj = getConnection().prepareStatement(createStatement);
            statementObj.executeUpdate();
            statementObj.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    /**
     * Uses an Order object and adds it to a row in the table
     *
     * @param o
     * @throws SQLException
     */
    public static void addOrderToTable(Order o)  {
        try{
            String statement = "insert into orders values(?,?,?,?,?)";
            PreparedStatement addStatement = getConnection().prepareStatement(statement);

            addStatement.setInt(1,o.getOrderId());
            addStatement.setString(2,o.getInstrument().toString());
            addStatement.setDouble(3,o.getInitialMarketPrice());
            addStatement.setLong(4,o.getClientId());
            addStatement.setInt(5,o.getSize());
            // Add order id from fix message when a fix message is made

            int addedStatement = addStatement.executeUpdate();
            if(addedStatement > 0){
                System.out.println("Order inserted");
            }else
                System.out.println("Record failed to add");

            addStatement.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    /**
     * Returns a ResultSet of every row in the Order table
     *
     * @return ResultSet
     * @throws SQLException
     */
    public static ResultSet selectAll()  {
        try{
            String selectStatement = "select * from orders;";
            PreparedStatement statement = getConnection().prepareStatement(selectStatement);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Takes the ResultSet from each row and prints it out
     *
     * @param rs
     * @throws SQLException
     */
    public static void printResultSet(ResultSet rs) throws SQLException {
        System.out.println("Contents of the Order table");
        while(rs.next()){
            System.out.println("-------------------------------");
            System.out.println("Order ID: "+rs.getString("order_id"));
            System.out.println("Instrument: "+rs.getString("ric"));
            System.out.println("Unit Price: "+rs.getString("unit_price"));
            System.out.println("Client ID: "+rs.getString("client_id"));
        }
    }

    /**
     * Removes an Order from a row in the Order table using an Order object (which uses the objects id)
     *
     * @param o
     * @throws SQLException
     */
    public static void removeOrderFromTable(Order o)  {
        try{
            String statementString = "delete from orders where order_id = ?";
            PreparedStatement deleteStatement = getConnection().prepareStatement(statementString);
            deleteStatement.setInt(1,o.getOrderId());
            deleteStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Removes all rows from the Orders table (used at end of project)
     *
     * @throws SQLException
     */
    public static void removeRowsFromTable()  {
        try{
            String deleteStatementString = "delete from orders";
            PreparedStatement deleteStatement = getConnection().prepareStatement(deleteStatementString);
            deleteStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public static Connection getConnection() {
        return connection;
    }

}
