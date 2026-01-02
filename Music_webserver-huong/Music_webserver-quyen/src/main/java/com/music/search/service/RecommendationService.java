package com.music.search.service;

import com.music.search.dto.SongDTO;
import com.music.search.entity.Song;
import com.music.search.entity.User;
import com.music.search.repository.SearchHistoryRepository;
import com.music.search.repository.SongRepository;
import com.music.search.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository; // THÊM DÒNG NÀY

    /**
     * Gợi ý bài hát dựa trên sở thích + lịch sử tìm kiếm
     * @param userId ID người dùng
     * @param limit Số lượng gợi ý tối đa
     * @return List SongDTO gợi ý
     */
    public List<SongDTO> getRecommendations(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            // Fallback: bài hát hot nhất nếu chưa login
            return songRepository.findTopSongsByViewCount(PageRequest.of(0, limit))
                    .stream()
                    .map(this::toDTO)
                    .toList();
        }

        Set<SongDTO> recommendations = new LinkedHashSet<>();

        // 1. Gợi ý từ nghệ sĩ yêu thích (chính xác nhất)
        if (user.getFavoriteArtists() != null && !user.getFavoriteArtists().isEmpty()) {
            String[] artists = user.getFavoriteArtists().split(",");
            for (String artist : artists) {
                artist = artist.trim();
                if (!artist.isEmpty()) {
                    songRepository.findByArtistContainingIgnoreCase(artist)
                            .forEach(song -> recommendations.add(toDTO(song)));
                }
            }
        }

        // 2. Gợi ý từ lịch sử tìm kiếm (mới nhất trước – học hành vi user)
        List<String> recentKeywords = searchHistoryRepository.findTop10KeywordsByUserId(userId);
        for (String keyword : recentKeywords) {
            if (!keyword.isEmpty()) {
                songRepository.searchByTitleOrArtist(keyword) // Dùng method search hiện có
                        .forEach(song -> recommendations.add(toDTO(song)));
            }
        }

        // 3. Fallback bài hát hot nếu ít kết quả
        if (recommendations.size() < limit) {
            songRepository.findTopSongsByViewCount(PageRequest.of(0, limit * 2))
                    .forEach(song -> recommendations.add(toDTO(song)));
        }

        // Limit và trả về
        return recommendations.stream()
                .limit(limit)
                .toList();
    }

    // Chuyển Song sang DTO (em điều chỉnh field theo SongDTO của em)
    private SongDTO toDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setArtist(song.getArtist());
        dto.setThumbnail(song.getThumbnail());
        dto.setYoutubeUrl(song.getYoutubeUrl());
        dto.setViewCount(song.getViewCount());
        // Thêm các field khác nếu cần
        return dto;
    }
}