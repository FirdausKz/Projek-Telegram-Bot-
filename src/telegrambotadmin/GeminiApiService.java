/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package telegrambotadmin;
import java.io.*;
import java.net.*;
import org.json.JSONObject;
/**
 *
 * @author espej
 */
public class GeminiApiService {
   
    private static final String API_KEY = "AIzaSyC44vfQ1zzJee6r8-_QcIJue1zbj5fR7T4";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;


    public static String getGeminiResponse(String userPrompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Bangun request payload
            JSONObject json = new JSONObject();
            JSONObject content = new JSONObject();
            content.put("parts", new org.json.JSONArray().put(new JSONObject().put("text", userPrompt)));
            json.put("contents", new org.json.JSONArray().put(content));
            
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("maxOutputTokens", 200); //menghemat token (~150 kata)
            json.put("generationConfig", generationConfig);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes("UTF-8"));
            }

            // Baca response
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Parse hasil response
            JSONObject responseObject = new JSONObject(response.toString());
            String geminiResponse = responseObject
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

            return geminiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "Maaf, terjadi kesalahan saat memproses permintaan AI.";
        }
    }
    
    
}
