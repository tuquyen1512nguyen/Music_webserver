// src/main/java/com/music/search/controller/AuthController.java
package com.music.search.controller;

import com.music.search.dto.*;
import com.music.search.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Hiển thị form đăng ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    // Xử lý đăng ký (gọi đúng AuthService của bạn)
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra mật khẩu nhập lại (vì RegisterRequest không có repassword)
        String repassword = request.getPassword(); // tạm dùng, sẽ lấy từ form riêng
        // → mình sẽ thêm field repassword ở form HTML

        // Nếu có lỗi validate
        if (result.hasErrors()) {
            return "register";
        }

        try {
            // Gọi đúng method register() của bạn → trả JwtResponse
            JwtResponse jwtResponse = authService.register(request);

            // Lưu token vào session hoặc localStorage (ở đây mình dùng flash + redirect)
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Đang chuyển hướng...");
            redirectAttributes.addFlashAttribute("token", jwtResponse.getToken());
            redirectAttributes.addFlashAttribute("username", jwtResponse.getUsername());

            return "redirect:/home"; // hoặc /home đã login

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // Trang login (giữ nguyên)
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}