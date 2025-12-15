package com.music.search.service.impl;

import com.music.search.entity.Playlist;
import com.music.search.entity.PlaylistSong;
import com.music.search.entity.Song;
import com.music.search.entity.User;
import com.music.search.repository.PlaylistRepository;
import com.music.search.repository.PlaylistSongRepository;
import com.music.search.repository.SongRepository;
import com.music.search.repository.UserRepository;
import com.music.search.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    @Override
    public Playlist createPlaylist(String name, String description, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Playlist playlist = Playlist.builder()
                .name(name)
                .description(description)
                .user(user)
                .build();
        return playlistRepository.save(playlist);
    }

    @Override
    public List<Playlist> getPlaylistsByUser(Long userId) {
        return playlistRepository.findByUserId(userId);
    }

    @Override
    public void addSongToPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        Song song = songRepository.findById(songId).orElseThrow();

        int maxPos = playlistSongRepository.findByPlaylistIdOrderByPosition(playlistId)
                .stream().mapToInt(PlaylistSong::getPosition).max().orElse(0);

        PlaylistSong ps = new PlaylistSong(playlist, song, maxPos + 1);
        playlistSongRepository.save(ps);
    }

    @Override
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    @Override
    public Playlist getPlaylistById(Long playlistId) {
        return playlistRepository.findById(playlistId).orElseThrow();
    }
}