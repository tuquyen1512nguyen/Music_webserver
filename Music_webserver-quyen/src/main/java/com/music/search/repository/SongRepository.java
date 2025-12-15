package com.music.search.repository;

import com.music.search.entity.Song;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    @Query("SELECT s FROM Song s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.artist) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Song> searchByTitleOrArtist(@Param("keyword") String keyword);

    // <<<--- SỬA DÒNG NÀY: THÊM Pageable làm tham số <<<
    @Query("SELECT s FROM Song s ORDER BY s.viewCount DESC")
    List<Song> findTop12ByOrderByViewCountDesc(Pageable pageable);

    @Query("SELECT s FROM Song s WHERE LOWER(s.lyric) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Song> searchByLyric(@Param("keyword") String keyword);

    // Method tiện lợi (giữ nguyên)
    default List<Song> findTop12ByOrderByViewCountDesc() {
        return findTop12ByOrderByViewCountDesc(PageRequest.of(0, 12));
    }
}