package com.music.search.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==================== SỞ THÍCH NGƯỜI DÙNG - CHO GỢI Ý BÀI HÁT ====================
    @Column(name = "favorite_genres", length = 500)
    private String favoriteGenres; // Ví dụ: "Pop,Rock,EDM,Ballad,V-Pop"

    @Column(name = "favorite_artists", length = 1000)
    private String favoriteArtists; // Ví dụ: "Alan Walker,Ed Sheeran,Sơn Tùng M-TP"

    public enum Role {
        USER, ADMIN
    }
}