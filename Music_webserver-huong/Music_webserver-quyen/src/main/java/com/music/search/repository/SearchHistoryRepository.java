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

    // 1. Top keywords theo user
    @Query("SELECT sh.keyword FROM SearchHistory sh WHERE sh.user.id = :userId " +
            "GROUP BY sh.keyword ORDER BY COUNT(sh.keyword) DESC")
    List<String> findTopKeywordsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Method tiện lợi
    default List<String> findTopKeywordsByUserId(Long userId, int limit) {
        return findTopKeywordsByUserId(userId, PageRequest.of(0, limit));
    }

    // 2. Top keywords toàn hệ thống (tất cả user)
    @Query("SELECT sh.keyword, COUNT(sh) as cnt FROM SearchHistory sh " +
            "GROUP BY sh.keyword " +
            "ORDER BY cnt DESC")
    List<Object[]> findTopKeywordsRaw(Pageable pageable);

    // Method tiện lợi trả về List<String>
    default List<String> findTopKeywords(int limit) {
        return findTopKeywordsRaw(PageRequest.of(0, limit))
                .stream()
                .map(row -> (String) row[0]) // row[0] là keyword, row[1] là count
                .collect(Collectors.toList());
    }
}