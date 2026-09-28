package com.discover.backend.tasteprofile;

import com.discover.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasteProfileRepository extends JpaRepository<TasteProfile, Long> {

    Optional<TasteProfile> findByUser(User user);
}
