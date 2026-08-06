package com.discover.backend.place;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/nearby")
    public List<PlaceDto> getPlacesWithinDistance(@RequestParam Double lat, @RequestParam Double lng, @RequestParam Double radius) {
        return placeService.getPlacesWithinDistance(lng, lat, radius);
    }

    @GetMapping("/search")
    public List<PlaceDto> getPlacesWithSearchFilter(@RequestParam(required = false) String name, @RequestParam(required = false) String category, @RequestParam(required = false) Integer budgetLevel) {
        return placeService.getPlacesBySearchFilter(name, category, budgetLevel);
    }
}
