package com.music.search.service;

import com.music.search.entity.SearchHistory;
import java.util.List;

public interface SearchHistoryService {
    void saveSearch(String keyword, Long userId);
    List<String> getTopKeywords(Long userId, int limit); // lấy top keyword hay tìm
}