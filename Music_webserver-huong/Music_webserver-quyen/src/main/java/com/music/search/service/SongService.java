package com.music.search.service;

import com.music.search.dto.SongDTO;

import java.util.List;

public interface SongService {
    List<SongDTO> searchSongs(String keyword);
    SongDTO getSongById(Long id);
    List<SongDTO> getTopSongsByViewCount(int limit);

}