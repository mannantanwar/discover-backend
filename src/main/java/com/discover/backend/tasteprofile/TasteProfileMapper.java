package com.discover.backend.tasteprofile;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TasteProfileMapper {

    TasteProfileDto toDto(TasteProfile tasteProfile);
}
