package com.music.search.repository;

import com.music.search.entity.SearchHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // <<<--- DÒNG QUAN TRỌNG: ĐẶT @QUERY Ở ĐÂY <<<
    @Query("SELECT sh.keyword FROM SearchHistory sh WHERE sh.user.id = :userId " +
            "GROUP BY sh.keyword ORDER BY COUNT(sh.keyword) DESC")
    List<String> findTopKeywordsByUserId(@Param("userId") Long userId, Pageable pageable);

    // Method tiện lợi để lấy top N
    default List<String> findTopKeywordsByUserId(Long userId, int limit) {
        return findTopKeywordsByUserId(userId, PageRequest.of(0, limit));
    }
}