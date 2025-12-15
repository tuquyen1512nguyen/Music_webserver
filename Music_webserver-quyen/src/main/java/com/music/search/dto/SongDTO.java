package com.music.search.dto;

import lombok.Data;

@Data
public class SongDTO {
    private Long id;
    private String title;
    private String artist;
    private String lyric;
    private String youtubeUrl;      // <-- BẮT BUỘC PHẢI CÓ DÒNG NÀY
    private String thumbnail;       // <-- BẮT BUỘC PHẢI CÓ DÒNG NÀY
    private int viewCount;
    private boolean isFavorite = false;
}