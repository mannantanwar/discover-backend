package com.discover.backend.place;

import com.discover.backend.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepo;
    private final PlaceMapper placeMapper;

    public List<PlaceDto> getAll() {
        return placeRepo.findAll().stream()
                .map(placeMapper::toDto)
                .toList();
    }

    public PlaceDto getByPublicId(UUID publicId) {
        Place place = getEntityByPublicId(publicId);
        return placeMapper.toDto(place);
    }

    public Place getEntityByPublicId(UUID publicId) {
        return placeRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found: " + publicId));
    }

    public List<PlaceDto> getPlacesWithinDistance(Double lng, Double lat, Double radius) {
        List<Place> placesWithin = placeRepo.findWithinDistance(lng, lat, radius);
        return placesWithin.stream()
                .map(placeMapper::toDto)
                .toList();
    }

    public List<PlaceDto> getPlacesBySearchFilter(String name , String category , Integer budgetLevel){
        List<Place>places = placeRepo.search(name , category, budgetLevel);
        return places.stream()
                .map(placeMapper :: toDto)
                .toList();
    }
}
