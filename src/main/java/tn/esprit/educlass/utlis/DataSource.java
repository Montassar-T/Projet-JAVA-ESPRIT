package tn.esprit.educlass.utlis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;

public class DataSource {
    private static DataSource dataSource;
    private Connection con;

    private String url;
    private String username;
    private String password;

    private DataSource() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("CRITICAL: Unable to find application.properties");
                return;
            }
            prop.load(input);

            this.url = prop.getProperty("db.url");
            this.username = prop.getProperty("db.username");
            this.password = prop.getProperty("db.password");

            con = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully.");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to connect to database: " + e.getMessage());
        }
    }

    public Connection getCon() {
        return con;
    }

    public static DataSource getInstance() {
        if (dataSource == null) {
            dataSource = new DataSource();
        }
        return dataSource;
    }
}
