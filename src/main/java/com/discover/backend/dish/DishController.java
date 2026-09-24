package com.discover.backend.dish;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DishController {

    private final DishService dishService;

    @GetMapping("/places/{placePublicId}/dishes")
    public List<DishDto> getDishesForPlace(@PathVariable UUID placePublicId) {
        return dishService.getDishesForPlace(placePublicId);
    }

    @GetMapping("/dishes/{publicId}")
    public DishDto getDishByPublicId(@PathVariable UUID publicId) {
        return dishService.getByPublicId(publicId);
    }
}
