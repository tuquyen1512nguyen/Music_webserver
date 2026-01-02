package com.music.search.controller;

import com.music.search.dto.SongDTO;
import com.music.search.entity.Playlist;
import com.music.search.entity.User;
import com.music.search.repository.UserRepository;
import com.music.search.service.FavoriteService;
import com.music.search.service.PlaylistService;
import com.music.search.service.RecommendationService; // THÊM IMPORT NÀY
import com.music.search.service.SearchHistoryService;
import com.music.search.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor // TỰ ĐỘNG INJECT CÁC FINAL FIELD
public class WebController {

    private final SongService songService;
    private final UserRepository userRepository;
    private final SearchHistoryService searchHistoryService;
    private final PlaylistService playlistService;
    private final FavoriteService favoriteService;
    private final PasswordEncoder passwordEncoder;
    private final RecommendationService recommendationService; // THÊM DÒNG NÀY – FIX LỖI

    // ==================== TRANG CHỦ ====================
    @GetMapping({"/", "/index"})
    public String index(Authentication auth, Model model) {
        // Trending chung cho mọi người
        List<SongDTO> featuredSongs = songService.getTopSongsByViewCount(12);
        model.addAttribute("featuredSongs", featuredSongs);

        // Gợi ý AI cá nhân hóa nếu đã login
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                List<SongDTO> aiRecommendations = recommendationService.getRecommendations(user.getId(), 12);
                model.addAttribute("recommendedSongs", aiRecommendations); // Tên attribute để frontend dùng
            }
        }

        return "index";
    }

    // ==================== TRANG KHÁM PHÁ ====================
    @GetMapping("/explore")
    public String explore(Model model) {
        List<SongDTO> trendingSongs = songService.getTopSongsByViewCount(12);
        model.addAttribute("trendingSongs", trendingSongs);
        return "explore";
    }

    // ==================== CHI TIẾT BÀI HÁT ====================
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

        // Gợi ý bài hát tương tự
        List<SongDTO> suggestedSongs = songService.getTopSongsByViewCount(10)
                .stream()
                .filter(s -> !s.getId().equals(id))
                .limit(4)
                .collect(Collectors.toList());

        model.addAttribute("suggestedSongs", suggestedSongs);

        return "song-detail";
    }

    // ==================== TÌM KIẾM ====================
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

    // ==================== THƯ VIỆN ====================
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

    // ==================== HỒ SƠ ====================
    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
            }
        }
        return "profile";
    }

    // Đổi mật khẩu
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

    // ==================== CẬP NHẬT SỞ THÍCH ÂM NHẠC ====================
    @PostMapping("/profile/update-preferences")
    public String updatePreferences(
            @RequestParam(required = false) List<String> favoriteGenres,
            @RequestParam(required = false) String favoriteArtists,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/profile";
        }

        // Cập nhật thể loại
        if (favoriteGenres != null && !favoriteGenres.isEmpty()) {
            StringJoiner joiner = new StringJoiner(",");
            for (String genre : favoriteGenres) {
                joiner.add(genre.trim());
            }
            user.setFavoriteGenres(joiner.toString());
        } else {
            user.setFavoriteGenres(null);
        }

        // Cập nhật nghệ sĩ
        user.setFavoriteArtists(favoriteArtists != null ? favoriteArtists.trim() : null);

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sở thích thành công! Gợi ý nhạc sẽ được cải thiện.");
        return "redirect:/profile";
    }

    // ==================== GẦN ĐÂY NGHE ====================
    @GetMapping("/recent")
    public String recent(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "recent"; // sẽ hiện thông báo đăng nhập
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "recent";

        // Lấy top 20 bài hát gần nhất user nghe (giả sử có entity ListenHistory hoặc từ view log)
        // Tạm thời: lấy top bài hát có viewCount cao + giả lập thời gian
        List<SongDTO> recentSongs = songService.getTopSongsByViewCount(20);

        model.addAttribute("recentSongs", recentSongs);
        return "recent";
    }
}