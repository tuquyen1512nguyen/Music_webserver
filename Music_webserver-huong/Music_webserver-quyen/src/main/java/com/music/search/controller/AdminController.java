package com.music.search.controller;

import com.music.search.entity.Song;
import com.music.search.entity.User;
import com.music.search.repository.PlaylistRepository;
import com.music.search.repository.SearchHistoryRepository;
import com.music.search.repository.SongRepository;
import com.music.search.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    // Redirect /admin → dashboard (khi gõ /admin)
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Tổng người dùng
        long totalUsers = userRepository.count();

        // Tổng bài hát
        long totalSongs = songRepository.count();

        // Tổng playlist
        long totalPlaylists = playlistRepository.count();

        // Bài hát hot nhất
        Song hotSong = songRepository.findTopSongByViewCount().orElse(null);

        // Người dùng mới hôm nay
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        long newUsersToday = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        // Top 10 từ khóa tìm kiếm
        List<String> topKeywords = searchHistoryRepository.findTopKeywords(10);

        // Truyền vào model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalSongs", totalSongs);
               model.addAttribute("totalPlaylists", totalPlaylists);
        model.addAttribute("hotSong", hotSong);
        model.addAttribute("newUsersToday", newUsersToday);
        model.addAttribute("topKeywords", topKeywords);

        return "admin/admin-dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/songs")
    public String manageSongs(Model model) {
        model.addAttribute("songs", songRepository.findAll());
        return "admin/songs";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/songs/delete/{id}")
    public String deleteSong(@PathVariable Long id) {
        songRepository.deleteById(id);
        return "redirect:/admin/songs";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        // Tăng trưởng người dùng tháng này (%)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        long usersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
        long usersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, startOfMonth.minusSeconds(1));

        double userGrowth = usersLastMonth == 0 ? 100.0 :
                ((double)(usersThisMonth - usersLastMonth) / usersLastMonth) * 100;
        model.addAttribute("userGrowth", Math.round(userGrowth * 10.0) / 10.0);

        // Tăng trưởng lượt nghe (dùng totalViews tháng này vs tháng trước – em thêm query nếu cần)
        model.addAttribute("viewGrowth", 0.0); // Tạm 0, em có thể thêm query tương tự

        // Thời gian nghe trung bình – tạm
        model.addAttribute("avgListenTime", "Chưa có dữ liệu");

        // Người dùng Việt Nam – tạm (em có thể thêm cột location nếu cần)
        model.addAttribute("vietnamUsers", 0);

        // Biểu đồ Line: lượt nghe 7 ngày gần nhất (dùng query hoặc giả lập nếu chưa có)
        // Em có thể thêm query daily views nếu cần – tạm giả lập
        model.addAttribute("dailyLabels", List.of("26/12", "27/12", "28/12", "29/12", "30/12", "31/12", "01/01"));
        model.addAttribute("dailyViews", List.of(12000L, 15000L, 18000L, 14000L, 16000L, 20000L, 22000L));

        // Top 5 bài hát hot
        List<Song> topSongs = songRepository.findTop5ByViewCount();
        model.addAttribute("topSongs", topSongs);

        // Phân bố vai trò
        long adminCount = userRepository.countByRole(User.Role.ADMIN);
        model.addAttribute("adminCount", adminCount);

        return "admin/statistics";
    }
}