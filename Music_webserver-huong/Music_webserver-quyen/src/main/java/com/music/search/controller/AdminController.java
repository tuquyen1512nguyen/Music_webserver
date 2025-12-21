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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

    // <<<--- THÊM METHOD NÀY ĐỂ KHI GÕ /admin → TỰ ĐỘNG VÀO DASHBOARD <<<
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

        // Tổng lượt nghe
        long totalViews = songRepository.findAll()
                .stream()
                .mapToLong(Song::getViewCount)
                .sum();

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
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalPlaylists", totalPlaylists);
        model.addAttribute("hotSong", hotSong);
        model.addAttribute("newUsersToday", newUsersToday);
        model.addAttribute("topKeywords", topKeywords);

        return "admin/admin-dashboard";
    }

    // Các trang khác...
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
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            // Có thể log lỗi
        }
        return "redirect:/admin/users";
    }
    @PostMapping("/songs/delete/{id}")
    public String deleteSong(@PathVariable Long id) {
        try {
            songRepository.deleteById(id);
        } catch (Exception e) {
            // Log lỗi nếu cần
        }
        return "redirect:/admin/songs";
    }
    @GetMapping("/statistics")
    public String statistics(Model model) {
        // 1. Tăng trưởng người dùng tháng này (%)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        long usersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
        long usersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);

        double userGrowth = usersLastMonth == 0 ? 100.0 :
                ((double)(usersThisMonth - usersLastMonth) / usersLastMonth) * 100;
        model.addAttribute("userGrowth", Math.round(userGrowth * 10.0) / 10.0);

        // 2. Tăng trưởng lượt nghe – tạm để 0 hoặc bỏ vì chưa có lịch sử
        model.addAttribute("viewGrowth", 0.0);

        // 3. Thời gian nghe trung bình – tạm
        model.addAttribute("avgListenTime", "Chưa có dữ liệu");

        // 4. Người dùng Việt Nam – tạm
        model.addAttribute("vietnamUsers", 0);

        // 5. Biểu đồ Line: Dữ liệu giả 7 ngày (vì chưa có lịch sử)
        List<String> dailyLabels = new ArrayList<>();
        List<Long> dailyViews = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dailyLabels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            dailyViews.add((long) (10000 + Math.random() * 20000)); // giả lập
        }
        model.addAttribute("dailyLabels", dailyLabels);
        model.addAttribute("dailyViews", dailyViews);

        // 6. Top 5 bài hát hot thật
        List<Song> topSongs = songRepository.findTop5ByOrderByViewCountDesc();
        model.addAttribute("topSongs", topSongs.isEmpty() ? Collections.emptyList() : topSongs);

        // 7. Phân bố vai trò
        long adminCount = userRepository.countByRole(User.Role.ADMIN);
        long userCount = userRepository.countByRole(User.Role.USER); // hoặc tổng - admin
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("userCount", userCount);

        return "admin/statistics";
    }
}