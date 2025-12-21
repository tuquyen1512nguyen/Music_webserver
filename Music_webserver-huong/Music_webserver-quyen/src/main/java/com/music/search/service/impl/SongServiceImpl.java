package com.music.search.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.music.search.dto.SongDTO;
import com.music.search.entity.Song;
import com.music.search.repository.SongRepository;
import com.music.search.service.LrcLibService;
import com.music.search.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final LrcLibService lrcLibService;           // Genius API mới
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api.key}")
    private String youtubeKey;

    @Override
    public List<SongDTO> searchSongs(String keyword) {
        List<Song> songs = new ArrayList<>();

        // Nếu keyword dài (> 10 ký tự) → ưu tiên tìm theo lời bài hát (người dùng nhớ lời)
        if (keyword.length() > 10) {
            songs = songRepository.searchByLyric(keyword);
        }

        // Nếu không tìm thấy hoặc keyword ngắn → tìm theo tên/ca sĩ như cũ
        if (songs.isEmpty()) {
            songs = songRepository.searchByTitleOrArtist(keyword);
        }

        // Kết hợp cả 2 để kết quả phong phú hơn (tránh bỏ sót)
        List<Song> byTitleArtist = songRepository.searchByTitleOrArtist(keyword);
        List<Song> byLyric = songRepository.searchByLyric(keyword);

        // Gộp + loại trùng + sắp xếp theo độ liên quan (lyric trước)
        Set<Song> combined = new LinkedHashSet<>(byLyric);
        combined.addAll(byTitleArtist);

        return combined.stream()
                .map(this::enrichSong)
                .collect(Collectors.toList());
    }

    @Override
    public SongDTO getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài hát không tồn tại"));

        // Tăng lượt xem
        song.setViewCount(song.getViewCount() + 1);

        // Lấy video + lời (nếu chưa có)
        enrichSong(song);
        songRepository.save(song);

        return mapToDTO(song);
    }
    @Override
    public List<SongDTO> getTopSongsByViewCount(int limit) {
        return songRepository.findTopByOrderByViewCountDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Bổ sung YouTube URL, thumbnail và lời bài hát (Genius) nếu chưa có
     */
    private SongDTO enrichSong(Song song) {
        boolean updated = false;

        // 1. YouTube video + thumbnail
        if (song.getYoutubeUrl() == null || song.getYoutubeUrl().isEmpty()) {
            String videoId = fetchYoutubeVideoId(song.getTitle(), song.getArtist());
            if (videoId != null) {
                song.setYoutubeUrl("https://www.youtube.com/embed/" + videoId + "?autoplay=1&rel=0&modestbranding=1");
                song.setThumbnail("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg");
                updated = true;
            }
        }

        // 2. Lời bài hát từ Genius API (chỉ lấy khi chưa có hoặc rỗng)
        if (song.getLyric() == null || song.getLyric().trim().isEmpty() || song.getLyric().contains("Không tìm thấy lời")) {
            String lyrics = lrcLibService.fetchLyrics(song.getTitle(), song.getArtist());
            if (lyrics != null && lyrics.trim().length() > 50) {  // Genius trả lời chất lượng cao
                song.setLyric(lyrics.trim() + "\n\n(Nguồn: Genius.com)");
                updated = true;
            }
        }

        // Lưu lại nếu có thay đổi
        if (updated) {
            songRepository.save(song);
        }

        return mapToDTO(song);
    }

    /**
     * Tìm video YouTube chính xác nhất
     */
    private String fetchYoutubeVideoId(String title, String artist) {
        if (youtubeKey == null || youtubeKey.isBlank()) return null;

        String[] queries = {
                title + " " + artist + " official music video",
                title + " " + artist + " official",
                title + " " + artist,
                title
        };

        for (String q : queries) {
            try {
                String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
                String url = "https://www.googleapis.com/youtube/v3/search" +
                        "?part=snippet&type=video&maxResults=1&q=" + encoded +
                        "&key=" + youtubeKey;

                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                JsonNode items = response.path("items");

                if (items.isArray() && items.size() > 0) {
                    String videoId = items.get(0).path("id").path("videoId").asText();
                    if (videoId != null && !videoId.isBlank()) {
                        return videoId;
                    }
                }
            } catch (Exception ignored) {
                // Thử query tiếp theo
            }
        }
        return null;
    }

    /**
     * Chuyển Entity → DTO
     */
    private SongDTO mapToDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setArtist(song.getArtist());
        dto.setLyric(song.getLyric());
        dto.setYoutubeUrl(song.getYoutubeUrl());
        dto.setThumbnail(song.getThumbnail());
        dto.setViewCount(song.getViewCount());
        return dto;
    }
}