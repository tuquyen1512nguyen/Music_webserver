package com.music.search.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlaylistSongId implements Serializable {
    private Long playlist;
    private Long song;
}