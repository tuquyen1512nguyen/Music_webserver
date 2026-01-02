package com.music.search.repository;

import com.music.search.entity.SearchHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Collectors;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // Lấy top 10 keyword gần đây nhất của user (recent – ưu tiên cho AI gợi ý hành vi mới)
    @Query("SELECT sh.keyword FROM SearchHistory sh WHERE sh.user.id = :userId ORDER BY sh.searchedAt DESC")
    List<String> findTop10KeywordsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Method tiện lợi gọi với limit 10
    default List<String> findTop10KeywordsByUserId(Long userId) {
        return findTop10KeywordsByUserId(userId, PageRequest.of(0, 10));
    }

    // Top keywords theo số lần tìm (count) – cho thống kê admin
    @Query("SELECT sh.keyword FROM SearchHistory sh WHERE sh.user.id = :userId " +
            "GROUP BY sh.keyword ORDER BY COUNT(sh.keyword) DESC")
    List<String> findTopKeywordsByCountByUserId(@Param("userId") Long userId, Pageable pageable);

    default List<String> findTopKeywordsByCountByUserId(Long userId, int limit) {
        return findTopKeywordsByCountByUserId(userId, PageRequest.of(0, limit));
    }

    // Top keywords toàn hệ thống (cho admin dashboard)
    @Query("SELECT sh.keyword, COUNT(sh.keyword) as cnt FROM SearchHistory sh " +
            "GROUP BY sh.keyword ORDER BY cnt DESC")
    List<Object[]> findTopKeywordsRaw(Pageable pageable);

    default List<String> findTopKeywords(int limit) {
        return findTopKeywordsRaw(PageRequest.of(0, limit))
                .stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());
    }
    // Lấy top N keyword gần đây nhất của user (ORDER BY searchedAt DESC)
    @Query("SELECT sh.keyword FROM SearchHistory sh WHERE sh.user.id = :userId ORDER BY sh.searchedAt DESC")
    List<String> findRecentKeywordsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Method tiện lợi lấy top 10 recent
    default List<String> findTop10RecentKeywords(Long userId) {
        return findRecentKeywordsByUserId(userId, PageRequest.of(0, 10));
    }
}