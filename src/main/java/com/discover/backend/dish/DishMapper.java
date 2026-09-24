package com.discover.backend.dish;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DishMapper {

    @Mapping(source = "place.publicId", target = "placePublicId")
    DishDto toDto(Dish dish);
}
