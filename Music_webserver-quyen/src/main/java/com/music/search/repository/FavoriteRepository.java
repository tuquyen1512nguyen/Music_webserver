package com.music.search.repository;

import com.music.search.entity.Favorite;
import com.music.search.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndSongId(Long userId, Long songId);

    void deleteByUserIdAndSongId(Long userId, Long songId);

    // <<<--- SỬA METHOD NÀY THÀNH @Query ĐỂ LẤY SONG TRỰC TIẾP
    @Query("SELECT f.song FROM Favorite f WHERE f.user.id = :userId ORDER BY f.id DESC")
    List<Song> findSongsByUserId(@Param("userId") Long userId);
}