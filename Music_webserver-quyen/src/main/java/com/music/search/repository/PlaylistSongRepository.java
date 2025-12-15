package com.music.search.repository;

import com.music.search.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Object> {
    List<PlaylistSong> findByPlaylistIdOrderByPosition(Long playlistId);
    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);
}
