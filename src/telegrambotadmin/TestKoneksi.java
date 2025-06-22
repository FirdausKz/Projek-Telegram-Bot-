/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package telegrambotadmin;

import java.sql.Connection;
import java.sql.SQLException;

public class TestKoneksi {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Koneksi database sukses!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

