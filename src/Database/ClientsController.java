package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientsController {
    private static Connection connection;

    public ClientsController(Connection connection){
        this.connection = connection;
    }

    /**
     * Creates a table of clients
     *
     * @throws SQLException
     */
    public static void createClientsTable()  {
        try {
            String createStatementString = "create table clients(client_id int not null,port_number int not null,constraint primary key (client_id));";
            PreparedStatement createStatement = getConnection().prepareStatement(createStatementString);
            createStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    /**
     * Adds a client to the table
     *
     * @param clientID
     * @param portNumber
     * @throws SQLException
     */
    public static void addClient(int clientID, int portNumber) {
        try{
            String insertString = "insert into clients values(?,?);";
            PreparedStatement insertStatement = getConnection().prepareStatement(insertString);
            insertStatement.setInt(1,clientID);
            insertStatement.setInt(2,portNumber);
            insertStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void removeClientFromTable(int clientID){
        try{
            String deleteString = "delete from clients where client_id = ?;";
            PreparedStatement deleteStatement = getConnection().prepareStatement(deleteString);
            deleteStatement.setInt(1,clientID);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
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
            String selectStatement = "select * from clients;";
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
        System.out.println("Content of the Client table");
        while(rs.next()){
            System.out.println("-------------------------------");
            System.out.println("Client ID: "+rs.getString("client_id"));
            System.out.println("Port Number: "+rs.getString("port_number"));
        }
    }

    /**
     * Removes all clients in the table
     *
     * @throws SQLException
     */
    public static void removeRowsFromTable()  {
        try{
            String deleteStatementString = "delete from clients";
            PreparedStatement deleteStatement = getConnection().prepareStatement(deleteStatementString);
            deleteStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection(Connection connection) {
        ClientsController.connection = connection;
    }
}
