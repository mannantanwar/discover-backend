package com.discover.backend.saved;

import com.discover.backend.place.Place;
import com.discover.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedRepository extends JpaRepository<Saved, Long> {

    Optional<Saved> findByUserAndPlace(User user, Place place);

    List<Saved> findAllByUser(User user);
}
