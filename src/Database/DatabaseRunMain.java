package Database;

import java.sql.*;

import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

public class DatabaseRunMain {

    public static void main(String[] args) throws Exception {
        DatabaseController dbc = new DatabaseController("com.mysql.jdbc.Driver","jdbc:mysql://localhost/marketdatabase","root","");
        //dbc.createOrderTable();
        Connection databaseConnection = dbc.getConnection();

        //String fix1 = "11=4;35=A;39=A";
        //FixController.createFixTable(databaseConnection);
        //FixController.addFixMessageToTable(databaseConnection,fix1);
        // CODE FOR TESTING ORDER TABLE

        Instrument testInstrument = new Instrument(new Ric("BT.L"));
        Order testOrder = new Order(4,2,testInstrument,5);
        Order testOrder2 = new Order(6,7,testInstrument,10);
        //dbc.addOrderToTable(testOrder);
        OrderController.addOrderToTable(databaseConnection,testOrder);
        OrderController.addOrderToTable(databaseConnection,testOrder2);
        //OrderController.removeOrderFromTable(databaseConnection,testOrder2);

        //

        //CODE FOR TESTING FIX TABLE

    }
}
