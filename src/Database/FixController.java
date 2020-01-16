package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Created by delegate on 15/01/2020.
 */
public class FixController {
    public static void createFixTable(Connection connection) throws SQLException {
        String createStatement = "create table fix_messages(client_order_id int not null unique, session_status varchar(1) not null, order_status varchar(20) not null,primary key (client_order_id));";
        PreparedStatement statementObj = connection.prepareStatement(createStatement);
        statementObj.executeUpdate();
        statementObj.close();
    }

    public static void addFixMessageToTable(Connection connection, String fixMessage) throws Exception {
        String[] fixMessageArr = fixMessage.split(";");
        String insertStatementString = "insert into fix_messages values(?,?,?)";
        PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);


        try{
            String[] fixValues = new String[3];
            for(int i = 0;i < fixMessageArr.length;i++){
                String fixTag = fixMessageArr[i];
                String[] tempFixValues = fixTag.split("=");
                fixValues[i] = tempFixValues[1];
                System.out.println(fixValues[i]);
            }
            insertStatement.setInt(1,Integer.parseInt(fixValues[0]));
            insertStatement.setString(2,fixValues[1]);
            insertStatement.setString(3,fixValues[2]);



            int insertedStatement = insertStatement.executeUpdate();

            if(insertedStatement > 0){
                System.out.println("Order inserted");
            }else
                System.out.println("Record failed to add");

            insertStatement.close();

        }catch(Exception e){
            System.out.println("Fix message will not be added");
            throw new Exception("Value is not parsable");
        }

    }

}
