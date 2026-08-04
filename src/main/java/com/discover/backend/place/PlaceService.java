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
        Place place = placeRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found: " + publicId));
        return placeMapper.toDto(place);
    }
}
