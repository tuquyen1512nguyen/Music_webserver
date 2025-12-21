package com.music.search.service;

import com.music.search.dto.JwtResponse;
import com.music.search.dto.LoginRequest;
import com.music.search.dto.RegisterRequest;

public interface AuthService {
    JwtResponse register(RegisterRequest request);
    JwtResponse login(LoginRequest request);
}