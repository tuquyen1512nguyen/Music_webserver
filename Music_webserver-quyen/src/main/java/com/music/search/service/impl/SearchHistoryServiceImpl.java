package com.music.search.service.impl;

import com.music.search.entity.SearchHistory;
import com.music.search.entity.User;
import com.music.search.repository.SearchHistoryRepository;
import com.music.search.repository.UserRepository;
import com.music.search.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryRepository repository;
    private final UserRepository userRepository; // <<<--- THÊM DÒNG NÀY ĐỂ LẤY USER

    @Override
    public void saveSearch(String keyword, Long userId) {
        if (keyword == null || keyword.trim().isEmpty()) return;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setKeyword(keyword.trim().toLowerCase());
        repository.save(history);
    }

    @Override
    public List<String> getTopKeywords(Long userId, int limit) {
        return repository.findTopKeywordsByUserId(userId, limit);
    }
}