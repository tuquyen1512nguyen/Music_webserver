package com.music.search.controller;

import com.music.search.entity.User;
import com.music.search.repository.UserRepository;
import com.music.search.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // <<<--- THÊM IMPORT NÀY
import org.springframework.web.bind.annotation.*;

@RestController // hoặc @Controller nếu em dùng @Controller ở các chỗ khác
@RequiredArgsConstructor // <<<--- QUAN TRỌNG: TỰ ĐỘNG INJECT
@RequestMapping("/favorite") // em có thể để ở đây hoặc không
public class FavoriteController { // anh khuyên tạo class riêng để sạch code

    private final UserRepository userRepository; // <<<--- INJECT
    private final FavoriteService favoriteService; // <<<--- INJECT

    @PostMapping("/toggle")
    @ResponseBody
    public String toggleFavorite(@RequestParam Long songId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "ERROR";
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "ERROR";
        }

        boolean added = favoriteService.toggleFavorite(songId, user.getId());
        return added ? "ADDED" : "REMOVED";
    }
}