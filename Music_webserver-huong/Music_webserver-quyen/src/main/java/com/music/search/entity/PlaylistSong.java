package com.music.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "playlist_songs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PlaylistSongId.class) // <<<--- DÒNG QUAN TRỌNG NHẤT – THÊM VÀO ĐÂY
public class PlaylistSong {

    @Id
    @ManyToOne
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @Id
    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    private int position; // thứ tự bài hát trong playlist
}

