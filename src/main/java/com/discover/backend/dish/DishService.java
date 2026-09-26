package com.discover.backend.dish;

import com.discover.backend.common.ResourceNotFoundException;
import com.discover.backend.event.EventService;
import com.discover.backend.place.Place;
import com.discover.backend.place.PlaceService;
import com.discover.backend.user.User;
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
    private final EventService eventService;

    public DishDto getByPublicId(UUID dishPublicId, User viewer) {
        Dish dish = getEntityByPublicId(dishPublicId);
        eventService.record(viewer, "DISH_VIEW", "DISH", dish.getId(), null);
        return dishMapper.toDto(dish);
    }

    public List<DishDto> getDishesForPlace(UUID placePublicId) {
        return getDishEntitiesForPlace(placePublicId).stream()
                .map(dishMapper::toDto)
                .toList();
    }

    public List<Dish> getDishEntitiesForPlace(UUID placePublicId) {
        Place place = placeService.getEntityByPublicId(placePublicId);
        return dishRepository.findAllByPlace(place);
    }

    public Dish getEntityByPublicId(UUID dishPublicId) {
        return dishRepository.findByPublicId(dishPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + dishPublicId));
    }
}
