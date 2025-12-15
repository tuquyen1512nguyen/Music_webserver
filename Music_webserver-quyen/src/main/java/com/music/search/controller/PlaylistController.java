package com.music.search.controller;

import com.music.search.entity.Playlist;
import com.music.search.entity.User;
import com.music.search.repository.UserRepository;
import com.music.search.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // <<<--- IMPORT ĐÚNG
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // <<<--- IMPORT ĐÚNG
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    // API tạo playlist mới + thêm bài ngay
    @PostMapping("/create-and-add")
    @ResponseBody
    public String createAndAdd(@RequestParam String name,
                               @RequestParam Long songId,
                               Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "ERROR"; // giờ hợp lệ

        User user = userRepository.findByUsername(auth.getName()).orElse(null); // giờ hợp lệ
        if (user == null) return "ERROR";

        Playlist playlist = playlistService.createPlaylist(name, "", user.getId());
        playlistService.addSongToPlaylist(playlist.getId(), songId);

        return "OK";
    }

    // API thêm bài vào playlist cũ
    @PostMapping("/add")
    @ResponseBody
    public String addSong(@RequestParam Long playlistId, @RequestParam Long songId) {
        playlistService.addSongToPlaylist(playlistId, songId);
        return "OK";
    }

    // Trang xem chi tiết playlist
    @GetMapping("/{id}")
    public String viewPlaylist(@PathVariable Long id, Model model) { // giờ hợp lệ
        Playlist playlist = playlistService.getPlaylistById(id);
        model.addAttribute("playlist", playlist); // giờ hợp lệ
        return "playlist/view";
    }
}