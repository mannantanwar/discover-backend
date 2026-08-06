package com.discover.backend.saved;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedRepository  extends JpaRepository<Long , Saved> {
    List<Place>findByUderID
}
