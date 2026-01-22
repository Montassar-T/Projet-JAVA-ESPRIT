package tn.esprit.educlass.utlis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static DataSource dataSource;
    private  Connection con;
    private  String url="jdbc:mysql://localhost:3306/educlass";
    private  String username="root";
    private  String password="";
    private DataSource (){
        try {
            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public Connection getCon() {
        return con;
    }

    public static DataSource getInstance(){
      if(dataSource==null){
          dataSource=new DataSource();
      }
        return dataSource;
    }
}
