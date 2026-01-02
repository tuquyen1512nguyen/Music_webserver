package com.music.search.service.impl;

import com.music.search.entity.SearchHistory;
import com.music.search.entity.User;
import com.music.search.repository.SearchHistoryRepository;
import com.music.search.repository.UserRepository;
import com.music.search.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryRepository repository;
    private final UserRepository userRepository;

    @Override
    public void saveSearch(String keyword, Long userId) {
        if (keyword == null || keyword.trim().isEmpty() || userId == null) return;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setKeyword(keyword.trim().toLowerCase());
        history.setSearchedAt(LocalDateTime.now()); // Nếu entity có field này
        repository.save(history);
    }

    // Lấy top keywords theo số lần tìm (count) – cho thống kê
    @Override
    public List<String> getTopKeywordsByCount(Long userId, int limit) {
        return repository.findTopKeywordsByCountByUserId(userId, limit);
    }

    // Lấy top keywords gần đây nhất (recent) – cho AI gợi ý hành vi mới
    @Override
    public List<String> getRecentKeywords(Long userId, int limit) {
        return repository.findRecentKeywordsByUserId(userId, Pageable.ofSize(limit));
    }

    // Method tiện lợi lấy top 10 recent (dùng cho AI)
    public List<String> getTop10RecentKeywords(Long userId) {
        return getRecentKeywords(userId, 10);
    }

}