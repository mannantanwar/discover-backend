package com.discover.backend.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByPublicId(UUID publicId);

    @Query(value = """
            select * from places p
            where ST_DWithin(p.location, ST_SetSRID(ST_makePoint(:longitude,:latitude),4326)::geography, :radiusMeters)
            order by ST_Distance(p.location, ST_SetSRID(ST_makePoint(:longitude,:latitude),4326)::geography)
            """, nativeQuery = true)
    List<Place> findWithinDistance(@Param("longitude") Double longitude ,@Param("latitude") Double latitude , @Param("radiusMeters") Double radiusMeters  );

    @Query(value = """
            select * from places p
            where (:name is null or p.name ilike '%' || :name || '%')
              and (:category is null or p.category = :category)
              and (:budgetLevel is null or p.budget_level = :budgetLevel)
            """, nativeQuery = true)
    List<Place> search(@Param("name") String name, @Param("category") String category, @Param("budgetLevel") Integer budgetLevel);

}
