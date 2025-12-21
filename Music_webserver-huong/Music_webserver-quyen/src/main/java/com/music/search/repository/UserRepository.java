package com.music.search.repository;

import com.music.search.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    // THÊM DÒNG NÀY LÀ HẾT LỖI NGAY!!!
    boolean existsByEmail(String email);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByCreatedAtAfter(LocalDateTime start);
    long countByRole(User.Role role);

    Optional<User> findByUsername(String username);
}