package com.music.search.service;

import com.music.search.entity.Playlist;

import java.util.List;

public interface PlaylistService {
    Playlist createPlaylist(String name, String description, Long userId);
    List<Playlist> getPlaylistsByUser(Long userId);
    void addSongToPlaylist(Long playlistId, Long songId);
    void removeSongFromPlaylist(Long playlistId, Long songId);
    Playlist getPlaylistById(Long playlistId);
}
