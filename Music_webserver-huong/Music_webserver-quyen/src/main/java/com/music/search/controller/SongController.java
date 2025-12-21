package com.music.search.controller;

import com.music.search.dto.SongDTO;
import com.music.search.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SongController {

    private final SongService songService;

    @GetMapping("/search")
    public ResponseEntity<List<SongDTO>> search(@RequestParam String keyword) {
        List<SongDTO> results = songService.searchSongs(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSong(@PathVariable Long id) {
        SongDTO song = songService.getSongById(id);
        return ResponseEntity.ok(song);
    }
}