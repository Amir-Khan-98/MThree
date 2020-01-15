package Database;

import java.sql.*;

import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

public class DatabaseRunMain {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DatabaseController dbc = new DatabaseController("com.mysql.jdbc.Driver","jdbc:mysql://localhost/marketdatabase","root","");
        //dbc.createOrderTable();
        Instrument testInstrument = new Instrument(new Ric("BT.L"));
        Order testOrder = new Order(4,2,testInstrument,5);
        Order testOrder2 = new Order(6,7,testInstrument,10);
        //dbc.addOrderToTable(testOrder);
        //DatabaseController.addOrderToTable(testOrder2);
        DatabaseController.removeOrderFromTable(testOrder2);

    }
}
