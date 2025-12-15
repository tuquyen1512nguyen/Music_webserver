package com.music.search.service;

import com.music.search.entity.Song;

import java.util.List;

public interface FavoriteService {
    boolean toggleFavorite(Long songId, Long userId);
    List<Song> getFavoriteSongs(Long userId);
    boolean isFavorite(Long songId, Long userId);
}