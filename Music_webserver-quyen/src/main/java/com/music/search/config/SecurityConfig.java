// src/main/java/com/music/search/config/SecurityConfig.java
package com.music.search.config;

import com.music.search.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService; // ← ĐÚNG 100%
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // hoặc bật + thêm token nếu muốn
                .authorizeHttpRequests(auth -> auth
                        // 1. CÁC TRANG CÔNG KHAI – AI CŨNG VÀO ĐƯỢC (KHÔNG CẦN LOGIN)
                        .requestMatchers(
                                "/", "/index",
                                "/search", "/songs/**",      // ← TÌM KIẾM + CHI TIẾT BÀI HÁT
                                "/explore",
                                "/css/**", "/js/**", "/images/**", "/fonts/**", "/img/**"
                        ).permitAll()

                        // 2. Trang đăng nhập, đăng ký
                        .requestMatchers("/login", "/register", "/api/auth/**").permitAll()

                        // 3. Các trang sau khi đăng nhập
                        .requestMatchers("/home", "/library").authenticated()

                        // 4. Admin (nếu có)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 5. Tất cả còn lại → yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}