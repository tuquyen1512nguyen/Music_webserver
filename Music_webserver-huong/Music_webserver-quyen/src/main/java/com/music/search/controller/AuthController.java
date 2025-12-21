package com.music.search.controller;

import com.music.search.dto.RegisterRequest;
import com.music.search.dto.JwtResponse;
import com.music.search.entity.User;
import com.music.search.repository.UserRepository;
import com.music.search.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; // Để kiểm tra trùng và lưu sở thích
    private final PasswordEncoder passwordEncoder;

    // ==================== TRANG ĐĂNG KÝ ====================
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    // ==================== XỬ LÝ ĐĂNG KÝ ====================
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            @RequestParam(required = false) String repassword, // Lấy mật khẩu nhập lại từ form
            Model model,
            RedirectAttributes redirectAttributes) {

        // Nếu có lỗi validate (username, password, email...)
        if (result.hasErrors()) {
            return "register";
        }

        // Kiểm tra mật khẩu nhập lại
        if (!request.getPassword().equals(repassword)) {
            model.addAttribute("error", "Mật khẩu nhập lại không khớp!");
            return "register";
        }

        // Kiểm tra username trùng
        if (userRepository.existsByUsername(request.getUsername())) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "register";
        }

        // Kiểm tra email trùng (nếu có email)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                model.addAttribute("error", "Email đã được sử dụng!");
                return "register";
            }
        }

        try {
            // Tạo user mới
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .role(User.Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            // ==================== LƯU SỞ THÍCH TỪ KHẢO SÁT ====================
            // Thể loại yêu thích (checkbox → List<String> → chuyển thành chuỗi cách nhau dấu phẩy)
            if (request.getFavoriteGenres() != null && !request.getFavoriteGenres().isEmpty()) {
                StringJoiner genresJoiner = new StringJoiner(",");
                for (String genre : request.getFavoriteGenres()) {
                    genresJoiner.add(genre.trim());
                }
                user.setFavoriteGenres(genresJoiner.toString());
            }

            // Nghệ sĩ yêu thích (text)
            if (request.getFavoriteArtists() != null && !request.getFavoriteArtists().trim().isEmpty()) {
                user.setFavoriteArtists(request.getFavoriteArtists().trim());
            }

            // Lưu vào DB
            userRepository.save(user);

            // Gọi authService để sinh token (nếu bạn dùng JWT)
            JwtResponse jwtResponse = authService.register(request);

            // Thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Hãy đăng nhập để trải nghiệm.");
            redirectAttributes.addFlashAttribute("token", jwtResponse.getToken());
            redirectAttributes.addFlashAttribute("username", jwtResponse.getUsername());

            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // ==================== TRANG ĐĂNG NHẬP ====================
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}