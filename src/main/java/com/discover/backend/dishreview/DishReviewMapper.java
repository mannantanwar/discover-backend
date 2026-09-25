package com.discover.backend.dishreview;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DishReviewMapper {

    @Mapping(source = "dish.publicId", target = "dishPublicId")
    DishReviewDto toDto(DishReview dishReview);
}
