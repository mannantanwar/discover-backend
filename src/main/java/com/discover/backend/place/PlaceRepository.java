package com.discover.backend.place;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByPublicId(UUID publicId);

}
