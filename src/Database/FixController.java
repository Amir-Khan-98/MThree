package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FixController {
    private static Connection connection;

    public FixController(Connection connection){
        this.connection = connection;
    }

    /**
     * Creates the FixMessage table
     *
     * @throws SQLException
     */
    public static void createFixTable()  {
        try{
            String createStatement = "create table fix_messages(fix_message_id int not null auto_increment,session_status varchar(1) not null,order_status varchar(1) not null,order_id int not null,constraint primary key(fix_message_id), constraint foreign key(order_id) references orders(order_id));";
            PreparedStatement statementObj = getConnection().prepareStatement(createStatement);
            statementObj.executeUpdate();
            statementObj.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    /**
     * Adds a fix message to the table using a string, if it is formated properly then it will work
     * if not it will throw and exception
     *
     * @param fixMessage
     * @throws SQLException
     */
    public static void addFixMessageToTable(String fixMessage)  {
        try{
            String insertStatementString = "insert into fix_messages(session_status,order_status,order_id) values(?,?,?)";
            PreparedStatement insertStatement = getConnection().prepareStatement(insertStatementString);
            String cOrderID = fixMessage.split(";")[0].split("=")[1];
            String orderStatus = fixMessage.split(";")[1].split("=")[1];
            String sessionStatus = fixMessage.split(";")[2].split("=")[1];

            System.out.println(cOrderID);

            insertStatement.setInt(3,Integer.parseInt(cOrderID));
            insertStatement.setString(2,orderStatus);
            insertStatement.setString(1,sessionStatus);

            int insertedStatement = insertStatement.executeUpdate();

            if(insertedStatement > 0){
                System.out.println("Order inserted");
            }else
                System.out.println("Record failed to add");

            insertStatement.close();

        }catch(SQLException e){
            e.printStackTrace();
            //System.out.println("Fix message will not be added");
            //throw new Exception("Value is not parsable");
        }

    }

    /**
     * Removes a message from the fix_messages table
     *
     * @param fixMessage
     * @throws SQLException
     */
    public static void removeMessageFromTable(String fixMessage)  {
        try{
            String clientOrderID = fixMessage.split(";")[0].split("=")[1];
            String deleteStatementString = "delete from fix_messages where order_id = ?;";
            PreparedStatement deleteStatement = getConnection().prepareStatement(deleteStatementString);
            deleteStatement.setInt(1, Integer.parseInt(clientOrderID));
            deleteStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Removes all fix messages in the table
     *
     * @throws SQLException
     */
    public static void removeRowsFromTable()  {
        try{
            String deleteStatementString = "delete from fix_messages";
            PreparedStatement deleteStatement = getConnection().prepareStatement(deleteStatementString);
            deleteStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Returns a ResultSet of every row in the Messages table
     *
     * @return ResultSet
     * @throws SQLException
     */
    public static ResultSet selectAll()  {
        try{
            String selectStatement = "select * from fix_messages;";
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
        System.out.println("Content of the Fix messages table");
        while(rs.next()){
            System.out.println("-------------------------------");
            System.out.println("Fix Message ID: "+rs.getString("fix_message_id"));
            System.out.println("Session Status: "+rs.getString("session_status"));
            System.out.println("Order Status: "+rs.getString("order_status"));
            System.out.println("Order ID: "+rs.getString("order_id"));
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
