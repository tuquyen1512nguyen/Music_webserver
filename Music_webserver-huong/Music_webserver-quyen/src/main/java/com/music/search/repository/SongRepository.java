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

    @Query("SELECT s FROM Song s WHERE LOWER(s.lyric) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Song> searchByLyric(@Param("keyword") String keyword);

    // Lấy top N bài hát theo viewCount (sửa đúng cách dùng Pageable)
    @Query("SELECT s FROM Song s ORDER BY s.viewCount DESC")
    List<Song> findTopSongsByViewCount(Pageable pageable);

    // Lấy top 12
    default List<Song> findTop12ByViewCount() {
        return findTopSongsByViewCount(PageRequest.of(0, 12));
    }

    // Lấy top 5
    default List<Song> findTop5ByViewCount() {
        return findTopSongsByViewCount(PageRequest.of(0, 5));
    }

    // Lấy top 1 (Optional)
    default Optional<Song> findTopSongByViewCount() {
        List<Song> top = findTopSongsByViewCount(PageRequest.of(0, 1));
        return top.isEmpty() ? Optional.empty() : Optional.of(top.get(0));
    }

    // Tổng view sau ngày
    @Query("SELECT COALESCE(SUM(s.viewCount), 0) FROM Song s WHERE s.updatedAt >= :date")
    long getTotalViewsAfter(@Param("date") LocalDateTime date);

    // Tổng view giữa 2 ngày
    @Query("SELECT COALESCE(SUM(s.viewCount), 0) FROM Song s WHERE s.updatedAt BETWEEN :start AND :end")
    long getTotalViewsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Method cho AI gợi ý
    List<Song> findByArtistContainingIgnoreCase(String artist);

    // Nếu Song có cột genre (em thêm nếu cần)
//    List<Song> findByGenreContainingIgnoreCase(String genre);

    // Method search tổng hợp (dùng cho lịch sử tìm kiếm)
    @Query("SELECT s FROM Song s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.artist) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.lyric) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Song> searchSongs(@Param("keyword") String keyword);
}