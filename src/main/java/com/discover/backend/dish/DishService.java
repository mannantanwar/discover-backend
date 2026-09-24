package com.discover.backend.dish;

import com.discover.backend.common.ResourceNotFoundException;
import com.discover.backend.place.Place;
import com.discover.backend.place.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final PlaceService placeService;

    public DishDto getByPublicId(UUID dishPublicId) {
        Dish dish = dishRepository.findByPublicId(dishPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + dishPublicId));
        return dishMapper.toDto(dish);
    }

    public List<DishDto> getDishesForPlace(UUID placePublicId) {
        Place place = placeService.getEntityByPublicId(placePublicId);
        return dishRepository.findAllByPlace(place).stream()
                .map(dishMapper::toDto)
                .toList();
    }
}
