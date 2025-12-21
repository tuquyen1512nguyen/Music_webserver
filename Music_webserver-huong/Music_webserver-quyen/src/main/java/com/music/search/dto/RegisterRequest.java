package com.music.search.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4-50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
    private String password;

    private String repassword; // Để validate nhập lại mật khẩu

    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    // === KHẢO SÁT SỞ THÍCH - CHO TÍNH NĂNG GỢI Ý SAU NÀY ===
    private List<String> favoriteGenres; // ví dụ: ["Pop", "Rock", "EDM"]

    private String favoriteArtists; // ví dụ: "Alan Walker, Ed Sheeran"

}