package com.music.search.controller;

import com.music.search.dto.SongDTO;
import com.music.search.entity.Playlist;
import com.music.search.entity.User;
import com.music.search.repository.UserRepository;
import com.music.search.service.FavoriteService;
import com.music.search.service.PlaylistService;
import com.music.search.service.SearchHistoryService;
import com.music.search.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final SongService songService;
    private final UserRepository userRepository;
    private final SearchHistoryService searchHistoryService;
    private final PlaylistService playlistService;
    private final FavoriteService favoriteService;
    private final PasswordEncoder passwordEncoder;

    // === TRANG NGƯỜI DÙNG ===

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/explore")
    public String explore() {
        return "explore";
    }

    @GetMapping("/songs/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication auth) {
        SongDTO song = songService.getSongById(id);
        if (song == null) {
            return "redirect:/";
        }

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

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         Authentication auth, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) {
            model.addAttribute("keyword", "");
            model.addAttribute("songs", Collections.emptyList());
        } else {
            keyword = keyword.trim();
            model.addAttribute("keyword", keyword);

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

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Authentication auth) {

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/profile";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng!");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không khớp!");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải ít nhất 6 ký tự!");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Thay đổi mật khẩu thành công!");

        return "redirect:/profile";
    }

    // === PHẦN ADMIN (DỮ LIỆU GIẢ ĐỂ TEST GIAO DIỆN) ===

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        // Dữ liệu giả để giao diện hiển thị đẹp
        model.addAttribute("totalUsers", 1234);
        model.addAttribute("totalSongs", 5678);
        model.addAttribute("totalViews", 9876543L);
        model.addAttribute("totalPlaylists", 890);
        model.addAttribute("newUsersToday", 45);

        SongDTO hotSong = new SongDTO();
        hotSong.setTitle("Faded");
        hotSong.setArtist("Alan Walker");
        hotSong.setViewCount(999999);
        model.addAttribute("hotSong", hotSong);

        model.addAttribute("topKeywords", List.of("Alan Walker", "Ed Sheeran", "Shape of You", "Faded", "On My Way"));

        return "admin/admin-dashboard";
    }
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        return "admin/admin-users"; // Sửa tương tự
    }

    @GetMapping("/admin/songs")
    public String adminSongs(Model model) {
        return "admin/admin-songs";
    }

    @GetMapping("/admin/statistics")
    public String adminStatistics(Model model) {
        return "admin/admin-statistics";
    }
    @GetMapping("/recent")
    public String recent() {
        return "recent";
    }
}