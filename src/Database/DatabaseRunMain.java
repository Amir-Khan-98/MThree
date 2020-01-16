package Database;

import java.sql.*;

import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

public class DatabaseRunMain {

    public static void main(String[] args) throws Exception {
        // PREREQUISITE FOR CONNECTING TO THE DATABASE AND CONTROLLERS
        DatabaseController dbc = new DatabaseController("com.mysql.jdbc.Driver","jdbc:mysql://localhost/marketdatabase","root","");
        Connection databaseConnection = dbc.getConnection();
        ClientsController clientsController = new ClientsController(databaseConnection);
        OrderController orderController = new OrderController(databaseConnection);
        FixController fixController = new FixController(databaseConnection);

        
        // CREATE THE TABLES
        //ClientsController.createClientsTable();
        //OrderController.createOrderTable();
        //FixController.createFixTable();


        //CODE FOR TESTING CLIENT TABLE
        //ClientsController.addClient(3,2020);
        //ClientsController.addClient(1,2021);
        //ClientsController.removeClientFromTable(0);
        ResultSet rs = ClientsController.selectAll();
        ClientsController.printResultSet(rs);


        // CODE FOR TESTING ORDER TABLE
        //Instrument testInstrument = new Instrument(new Ric("BT.L"));
        //Order testOrder = new Order(3,2,testInstrument,5); //Random Order object 1
        //Order testOrder2 = new Order(6,7,testInstrument,10); //Random Order object 1

        //OrderController.addOrderToTable(testOrder);
        //OrderController.addOrderToTable(testOrder2);
        //OrderController.removeOrderFromTable(testOrder2);

        ResultSet orderResults = OrderController.selectAll();
        OrderController.printResultSet(orderResults);



        // CODE FOR TESTING FIX TABLE
        //String fix1 = "11=0;35=A;39=A";
        //FixController.addFixMessageToTable(fix1);
        //FixController.removeMessageFromTable(fix1);
        //FixController.removeRowsFromTable();

        ResultSet fixResults = FixController.selectAll();
        FixController.printResultSet(fixResults);
    }
}
