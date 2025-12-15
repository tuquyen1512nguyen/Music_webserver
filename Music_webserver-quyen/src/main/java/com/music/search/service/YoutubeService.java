package com.music.search.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class YoutubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Thử nhiều query để chắc chắn tìm được video
     */
    @Cacheable(value = "youtubeCache", key = "#title + #artist")
    public String searchVideoId(String title, String artist) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR")) {
            return null;
        }

        String[] queries = new String[]{
                title + " " + artist + " official MV",
                title + " " + artist,
                title
        };

        for (String q : queries) {
            try {
                String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
                String url = "https://www.googleapis.com/youtube/v3/search" +
                        "?part=snippet" +
                        "&q=" + encoded +
                        "&type=video" +
                        "&maxResults=1" +
                        "&key=" + apiKey;

                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.get("items") != null) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (!items.isEmpty()) {
                        Map<String, Object> idMap = (Map<String, Object>) items.get(0).get("id");
                        String videoId = (String) idMap.get("videoId");
                        if (videoId != null) {
                            System.out.println("Found videoId for \"" + title + "\": " + videoId);
                            return videoId;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("YouTube API lỗi: " + e.getMessage());
            }
        }

        System.out.println("Không tìm thấy video cho: " + title);
        return null;
    }

    public String getEmbedUrl(String videoId) {
        if (videoId == null) return null;
        return "https://www.youtube.com/embed/" + videoId + "?autoplay=1";
    }

    public String getThumbnailUrl(String videoId) {
        if (videoId == null) return null;
        return "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
    }

}

