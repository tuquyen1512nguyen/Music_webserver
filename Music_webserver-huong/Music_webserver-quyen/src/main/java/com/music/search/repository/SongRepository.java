package com.music.search.repository;

import com.music.search.entity.Song;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {

    @Query("SELECT s FROM Song s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.artist) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Song> searchByTitleOrArtist(@Param("keyword") String keyword);

    // <<<--- SỬA DÒNG NÀY: THÊM Pageable làm tham số <<<
    @Query("SELECT s FROM Song s ORDER BY s.viewCount DESC")
    List<Song> findTop12ByOrderByViewCountDesc(Pageable pageable);

    @Query("SELECT s FROM Song s WHERE LOWER(s.lyric) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Song> searchByLyric(@Param("keyword") String keyword);

    @Query("SELECT s FROM Song s ORDER BY s.viewCount DESC")
    List<Song> findTopByOrderByViewCountDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.viewCount), 0) FROM Song s WHERE s.updatedAt >= :date")
    long getTotalViewsAfter(@Param("date") LocalDateTime date);

    @Query("SELECT COALESCE(SUM(s.viewCount), 0) FROM Song s WHERE s.updatedAt BETWEEN :start AND :end")
    long getTotalViewsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Song> findTop5ByOrderByViewCountDesc();

    // Method tiện lợi lấy top 1 (trả Optional<Song>)
    default Optional<Song> findTopSongByViewCount() {
        Pageable topOne = PageRequest.of(0, 12);
        List<Song> topList = findTopByOrderByViewCountDesc(topOne);
        return topList.isEmpty() ? Optional.empty() : Optional.of(topList.get(0));
    }
    List<Song> findByArtistContainingIgnoreCase(String artist);
}