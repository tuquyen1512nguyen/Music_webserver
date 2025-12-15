package com.music.search.controller;

import com.music.search.dto.SongDTO;
import com.music.search.entity.Playlist;
import com.music.search.entity.Song;
import com.music.search.entity.User;
import com.music.search.repository.SongRepository;
import com.music.search.repository.UserRepository;
import com.music.search.service.FavoriteService;
import com.music.search.service.PlaylistService;
import com.music.search.service.SearchHistoryService;
import com.music.search.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final SongService songService;
    private final SongRepository songRepository;           // <<<--- THÊM
    private final UserRepository userRepository;           // <<<--- THÊM
    private final SearchHistoryService searchHistoryService; // <<<--- THÊM
    private final PlaylistService playlistService;
    private final FavoriteService favoriteService;
    // TRANG CHỦ + GỢI Ý NỔI BẬT DỰA TRÊN LỊCH SỬ
    @GetMapping({"/", "/index", "/home"})
    public String home(Authentication auth, Model model) {
        List<SongDTO> featuredSongs = new ArrayList<>();

        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<String> topKeywords = searchHistoryService.getTopKeywords(user.getId(), 5);
                if (!topKeywords.isEmpty()) {
                    featuredSongs = topKeywords.stream()
                            .flatMap(kw -> songService.searchSongs(kw).stream())
                            .distinct()
                            .limit(12)
                            .collect(Collectors.toList());
                }
            }
        }

        // Nếu chưa login hoặc chưa có lịch sử → lấy top viewCount
        if (featuredSongs.isEmpty()) {
            featuredSongs = songRepository.findTop12ByOrderByViewCountDesc(PageRequest.of(0, 12))
                    .stream()
                    .map(this::mapToDTO)  // dùng method dưới đây
                    .collect(Collectors.toList());
        }

        model.addAttribute("featuredSongs", featuredSongs);
        return "index"; // hoặc "home" tùy em dùng trang nào làm chủ
    }

    // TÌM KIẾM + LƯU LỊCH SỬ (nếu đã login)
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         Authentication auth, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) {
            model.addAttribute("keyword", "");
            model.addAttribute("songs", Collections.emptyList());
        } else {
            keyword = keyword.trim();
            model.addAttribute("keyword", keyword);

            // Lưu lịch sử nếu đã login
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    searchHistoryService.saveSearch(keyword, user.getId());
                }
            }

            List<SongDTO> songs = songService.searchSongs(keyword);
            model.addAttribute("songs", songs);
        }
        return "search-results";
    }


    @GetMapping("/explore")
    public String explore() {
        return "explore";
    }

    // METHOD CHUYỂN ENTITY SANG DTO (để dùng ở top viewCount)
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
    @GetMapping("/songs/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication auth) {
        SongDTO song = songService.getSongById(id);
        model.addAttribute("song", song);
        boolean isFavorite = false;

        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("myPlaylists", playlistService.getPlaylistsByUser(user.getId()));
                isFavorite = favoriteService.isFavorite(id, user.getId());
            }
        }
        model.addAttribute("isFavorite", isFavorite);


        return "song-detail";
    }

    //TRANG LIBRARY
    @GetMapping("/library")
    public String library(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null) {
            List<Playlist> myPlaylists = playlistService.getPlaylistsByUser(user.getId());
            model.addAttribute("myPlaylists", myPlaylists);
            model.addAttribute("favoriteSongs", favoriteService.getFavoriteSongs(user.getId()));
        }

        return "library";
    }

    // Các method khác của em (home, search, explore, v.v.)
}