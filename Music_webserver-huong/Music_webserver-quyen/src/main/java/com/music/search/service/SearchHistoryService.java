package com.music.search.service;

import java.util.List;

public interface SearchHistoryService {

    /**
     * Lưu lịch sử tìm kiếm (keyword) cho user
     * @param keyword từ khóa tìm kiếm
     * @param userId ID người dùng (null nếu chưa login)
     */
    void saveSearch(String keyword, Long userId);

    /**
     * Lấy top keywords theo số lần tìm (count) – cho thống kê
     * @param userId ID người dùng
     * @param limit số lượng
     * @return List<String> keyword
     */
    List<String> getTopKeywordsByCount(Long userId, int limit);

    /**
     * Lấy top keywords gần đây nhất (recent) – cho AI gợi ý hành vi mới
     * @param userId ID người dùng
     * @param limit số lượng
     * @return List<String> keyword
     */
    List<String> getRecentKeywords(Long userId, int limit);

    /**
     * Method tiện lợi lấy top 10 recent keywords (dùng cho AI)
     */
    default List<String> getTop10RecentKeywords(Long userId) {
        return getRecentKeywords(userId, 10);
    }

}