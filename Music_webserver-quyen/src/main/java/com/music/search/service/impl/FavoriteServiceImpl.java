package com.music.search.service.impl;

import com.music.search.entity.Favorite;
import com.music.search.entity.Song;
import com.music.search.entity.User;
import com.music.search.repository.FavoriteRepository;
import com.music.search.service.FavoriteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Override
    @Transactional
    public boolean toggleFavorite(Long songId, Long userId) {
        if (favoriteRepository.existsByUserIdAndSongId(userId, songId)) {
            favoriteRepository.deleteByUserIdAndSongId(userId, songId);
            return false;
        } else {
            Favorite favorite = new Favorite();
            User user = new User();
            user.setId(userId);
            favorite.setUser(user);

            Song song = new Song();
            song.setId(songId);
            favorite.setSong(song);

            favoriteRepository.save(favorite);
            return true;
        }
    }

    @Override
    public List<Song> getFavoriteSongs(Long userId) {
        return favoriteRepository.findSongsByUserId(userId);
    }

    @Override
    public boolean isFavorite(Long songId, Long userId) {
        return favoriteRepository.existsByUserIdAndSongId(userId, songId);
    }
}
