package com.discover.backend.dish;

import com.discover.backend.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, Long> {

    Optional<Dish> findByPublicId(UUID publicId);

    List<Dish> findAllByPlace(Place place);

    @Query(value = "SELECT DISTINCT tag FROM dishes, unnest(taste_tags) AS tag ORDER BY tag", nativeQuery = true)
    List<String> findDistinctTasteTags();

    // one query for every dish at every place in range, instead of one query per nearby place
    @Query(value = """
            SELECT d.* FROM dishes d
            JOIN places p ON d.place_id = p.id
            WHERE ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters)
            """, nativeQuery = true)
    List<Dish> findWithinDistance(@Param("longitude") Double longitude,
                                  @Param("latitude") Double latitude,
                                  @Param("radiusMeters") Double radiusMeters);
}
