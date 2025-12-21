// src/main/java/com/music/search/service/LrcLibService.java
package com.music.search.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class LrcLibService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://lrclib.net/api/get";

    public String fetchLyrics(String title, String artist) {
        if (title == null || title.trim().isEmpty()) return null;

        String url = API_URL + "?track_name=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
        if (artist != null && !artist.trim().isEmpty()) {
            url += "&artist_name=" + URLEncoder.encode(artist, StandardCharsets.UTF_8);
        }

        try {
            Thread.sleep(300); // lịch sự thôi

            String json = restTemplate.getForObject(url, String.class);

            if (json == null || json.contains("\"syncedLyrics\":null") && json.contains("\"plainLyrics\":null")) {
                return null;
            }

            // Lấy lời có timestamp trước (ưu tiên)
            String synced = extract(json, "\"syncedLyrics\":\"", "\"");
            if (synced != null && !synced.isEmpty()) {
                return cleanLyrics(synced) + "\n\n(Nguồn: lrclib.net)";
            }

            // Nếu không có synced thì lấy plain
            String plain = extract(json, "\"plainLyrics\":\"", "\"");
            if (plain != null && !plain.isEmpty()) {
                return cleanLyrics(plain) + "\n\n(Nguồn: lrclib.net)";
            }

        } catch (Exception e) {
            System.out.println("LrcLib tạm thời không lấy được: " + e.getMessage());
        }
        return null;
    }

    private String extract(String json, String start, String end) {
        try {
            int s = json.indexOf(start);
            if (s == -1) return null;
            s += start.length();
            int e = json.indexOf(end, s);
            if (e == -1) return null;
            return json.substring(s, e).replace("\\n", "\n").replace("\\/", "/");
        } catch (Exception ex) {
            return null;
        }
    }

    private String cleanLyrics(String lyrics) {
        return lyrics.trim()
                .replace("\\n", "\n")
                .replace("\n\n\n", "\n\n")
                .replaceAll("(?m)^\\s+$", "");
    }
}