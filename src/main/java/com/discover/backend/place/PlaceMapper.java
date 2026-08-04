package com.discover.backend.place;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlaceMapper {

    @Mapping(target = "latitude", expression = "java(place.getLocation().getY())")
    @Mapping(target = "longitude", expression = "java(place.getLocation().getX())")
    PlaceDto toDto(Place place);
}
