package com.music.search.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.music.search.dto.SongDTO;
import com.music.search.entity.Song;
import com.music.search.entity.User;
import com.music.search.repository.SongRepository;
import com.music.search.repository.UserRepository;
import com.music.search.service.LrcLibService;
import com.music.search.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final UserRepository userRepository;
    private final LrcLibService lrcLibService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api.key:}")
    private String youtubeKey;

    // ===================== TÌM KIẾM BÀI HÁT =====================
    @Override
    public List<SongDTO> searchSongs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        keyword = keyword.trim();

        List<Song> byLyric = new ArrayList<>();
        List<Song> byTitleArtist = songRepository.searchByTitleOrArtist(keyword);

        if (keyword.length() > 10) {
            byLyric = songRepository.searchByLyric(keyword);
        }

        Set<Song> combined = new LinkedHashSet<>(byLyric);
        combined.addAll(byTitleArtist);

        return combined.stream()
                .map(this::enrichSong)
                .collect(Collectors.toList());
    }

    // ===================== LẤY BÀI HÁT THEO ID =====================
    @Override
    public SongDTO getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài hát không tồn tại"));

        song.setViewCount(song.getViewCount() + 1);
        enrichSong(song);
        songRepository.save(song);

        return mapToDTO(song);
    }

    // ===================== TOP BÀI HÁT THEO LƯỢT XEM =====================
    @Override
    public List<SongDTO> getTopSongsByViewCount(int limit) {
        return songRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount")))
                .getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===================== GỢI Ý DỰA TRÊN SỞ THÍCH NGƯỜI DÙNG =====================
    @Override
    public List<SongDTO> getRecommendedSongsByPreference(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);

        // Nếu không tìm thấy user hoặc chưa có sở thích → gợi ý top hot nhất
        if (user == null ||
                (user.getFavoriteArtists() == null || user.getFavoriteArtists().trim().isEmpty()) &&
                        (user.getFavoriteGenres() == null || user.getFavoriteGenres().trim().isEmpty())) {

            return getTopSongsByViewCount(limit);
        }

        List<Song> recommended = new ArrayList<>();

        // Ưu tiên nghệ sĩ yêu thích
        if (user.getFavoriteArtists() != null && !user.getFavoriteArtists().trim().isEmpty()) {
            String[] artists = user.getFavoriteArtists().split(",");
            for (String artist : artists) {
                artist = artist.trim();
                if (!artist.isEmpty()) {
                    recommended.addAll(songRepository.findByArtistContainingIgnoreCase(artist));
                }
            }
        }

        // (Tương lai: thêm theo thể loại nếu Song có field genre)
        // if (user.getFavoriteGenres() != null && !user.getFavoriteGenres().trim().isEmpty()) { ... }

        // Loại trùng, giới hạn, enrich dữ liệu
        return recommended.stream()
                .distinct()
                .limit(limit)
                .map(this::enrichSong)
                .collect(Collectors.toList());
    }

    // ===================== BỔ SUNG DỮ LIỆU (YouTube + Lời) =====================
    private SongDTO enrichSong(Song song) {
        boolean updated = false;

        if ((song.getYoutubeUrl() == null || song.getYoutubeUrl().isEmpty()) && youtubeKey != null && !youtubeKey.isBlank()) {
            String videoId = fetchYoutubeVideoId(song.getTitle(), song.getArtist());
            if (videoId != null) {
                song.setYoutubeUrl("https://www.youtube.com/embed/" + videoId + "?autoplay=1&rel=0");
                song.setThumbnail("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
                updated = true;
            }
        }

        if (song.getLyric() == null || song.getLyric().trim().isEmpty() || song.getLyric().contains("Không tìm thấy")) {
            String lyrics = lrcLibService.fetchLyrics(song.getTitle(), song.getArtist());
            if (lyrics != null && lyrics.trim().length() > 50) {
                song.setLyric(lyrics.trim());
                updated = true;
            }
        }

        if (updated) {
            songRepository.save(song);
        }

        return mapToDTO(song);
    }

    // ===================== TÌM VIDEO YOUTUBE =====================
    private String fetchYoutubeVideoId(String title, String artist) {
        if (youtubeKey == null || youtubeKey.isBlank()) return null;

        String[] queries = {
                title + " " + artist + " official music video",
                title + " " + artist + " official audio",
                title + " " + artist,
                title
        };

        for (String q : queries) {
            try {
                String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
                String url = "https://www.googleapis.com/youtube/v3/search"
                        + "?part=snippet&type=video&maxResults=3&q=" + encoded
                        + "&key=" + youtubeKey;

                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response == null) continue;

                JsonNode items = response.path("items");
                if (items.isArray() && items.size() > 0) {
                    String videoId = items.get(0).path("id").path("videoId").asText();
                    if (videoId != null && !videoId.isBlank()) {
                        return videoId;
                    }
                }
            } catch (Exception ignored) {
                // Tiếp tục query khác
            }
        }
        return null;
    }

    // ===================== MAP ENTITY → DTO =====================
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