/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package telegrambotadmin;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyTelegramBot extends TelegramLongPollingBot {

 @Override
public void onUpdateReceived(Update update) {
     if (update.hasMessage()) {
        String chatId = update.getMessage().getChatId().toString();

        // 1️⃣ Cek dulu apakah user mengirim Contact:
        if (update.getMessage().hasContact()) {
            String phoneNumber = update.getMessage().getContact().getPhoneNumber();

            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "UPDATE member SET nomor_hp=? WHERE chat_id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, phoneNumber);
                pst.setString(2, chatId);
                pst.executeUpdate();
                pst.close();

                SendMessage msg = new SendMessage(chatId, "Nomor HP Anda sudah berhasil disimpan.");
                execute(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;  // selesai setelah menerima kontak
        }

        // 2️⃣ Setelah cek contact, baru kita proses pesan teks:
        if (update.getMessage().hasText()) {
            String pesan = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();

            // Cek apakah sudah terdaftar
            if (!sudahTerdaftar(chatId)) {
                simpanMemberBaru(chatId, username);
            }

            // Simpan history
            simpanHistory(chatId, pesan);

            if (pesan.equalsIgnoreCase("/menu")) {
                String daftarMenu = getAllKeywords();
                SendMessage msg = new SendMessage(chatId, daftarMenu);
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }

            cekKeyword(chatId, pesan);
        }
    }
}
private void simpanMemberBaru(String chatId, String username) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO member (nomor_hp, username, chat_id) VALUES (?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, "-");
        pst.setString(2, username != null ? username : "-");
        pst.setString(3, chatId);
        pst.executeUpdate();
        pst.close();

        // Kirim pesan selamat datang
        SendMessage msg = new SendMessage(chatId, "Selamat datang! Anda sudah terdaftar.");
        execute(msg);

        // Tambahkan baris ini untuk minta kontak
        mintaKontak(chatId);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

   
private void mintaKontak(String chatId) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("Silakan kirim nomor HP Anda dengan menekan tombol di bawah:");

    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);
    keyboardMarkup.setOneTimeKeyboard(true);

    KeyboardButton contactButton = new KeyboardButton();
    contactButton.setText("Kirim Nomor HP Saya");
    contactButton.setRequestContact(true);

    KeyboardRow row = new KeyboardRow();
    row.add(contactButton);

    List<KeyboardRow> keyboard = new ArrayList<>();
    keyboard.add(row);
    keyboardMarkup.setKeyboard(keyboard);

    message.setReplyMarkup(keyboardMarkup);

    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}

private boolean sudahTerdaftar(String chatId) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM member WHERE chat_id = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, chatId);
        ResultSet rs = pst.executeQuery();
        boolean ada = rs.next();
        rs.close();
        pst.close();
        return ada;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
public String getAllKeywords() {
        StringBuilder daftar = new StringBuilder("Berikut daftar menu:\n");
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT keyword FROM keyword";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                String keyword = rs.getString("keyword");
                daftar.append(keyword).append("\n");
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return daftar.toString();
    }



private void cekKeyword(String chatId, String pesan) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT jawaban FROM keyword WHERE keyword = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, pesan);  // pesan mengandung /start, /info, dll
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            String jawaban = rs.getString("jawaban");
            SendMessage msg = new SendMessage(chatId, jawaban);
            execute(msg);
        } else {
            SendMessage msg = new SendMessage(chatId, "Maaf, perintah tidak dikenal.");
            execute(msg);
        }

        rs.close();
        pst.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void kirimMenuUtama(String chatId) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("Silakan pilih menu yang tersedia:");

    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    keyboardMarkup.setResizeKeyboard(true);

    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row = new KeyboardRow();

    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT keyword FROM keyword";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        int count = 0;
        while (rs.next()) {
            String keyword = rs.getString("keyword");

            row.add(keyword);

            count++;
            // supaya max 3 tombol per baris
            if (count % 3 == 0) {
                keyboard.add(row);
                row = new KeyboardRow();
            }
        }

        // tambahkan sisa jika ada
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        rs.close();
        st.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }

    keyboardMarkup.setKeyboard(keyboard);
    message.setReplyMarkup(keyboardMarkup);

    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}





private void simpanHistory(String chatId, String pesan) {
    try {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO history (chat_id, pesan) VALUES (?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, chatId);
        pst.setString(2, pesan);
        pst.executeUpdate();
        pst.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    @Override
    public String getBotUsername() {
        return "GemZero_bot"; // ganti dengan username bot kamu
    }

    @Override
    public String getBotToken() {
        return "8199367656:AAEqhEcQ4hLGWHAozUiWxqllstRXtdQxwtY"; // ganti dengan token dari BotFather
    }
    
    
    public void kirimPesan(String chatId, String pesan) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(pesan);
    try {
        execute(message);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}

}

