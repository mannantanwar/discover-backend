package com.discover.backend.dish;

import com.discover.backend.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, Long> {

    Optional<Dish> findByPublicId(UUID publicId);

    List<Dish> findAllByPlace(Place place);
}
