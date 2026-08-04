package com.discover.backend.place;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public List<PlaceDto> getAllPlaces() {
        return placeService.getAll();
    }

    @GetMapping("/{publicId}")
    public PlaceDto getPlaceByPublicId(@PathVariable UUID publicId) {
        return placeService.getByPublicId(publicId);
    }
}
