/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package telegrambotadmin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:mysql://localhost:3306/telegrambotadmin";
            String user = "root";  // Sesuaikan username MySQL
            String password = "";  // Sesuaikan password MySQL
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
}

