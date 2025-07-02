/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package telegrambotadmin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *
 * @author espej
 */
public class keywordService {
    public String cariJawaban(String pesan) {
        String jawaban = null;
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT jawaban FROM keyword WHERE keyword = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, pesan);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                jawaban = rs.getString("jawaban");
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jawaban;
    }
}
