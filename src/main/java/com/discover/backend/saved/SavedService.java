package com.discover.backend.saved;

import com.discover.backend.place.Place;
import com.discover.backend.place.PlaceDto;
import com.discover.backend.place.PlaceMapper;
import com.discover.backend.place.PlaceService;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedService {

    private final SavedRepository savedRepo;
    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    public PlaceDto save(User user, UUID placePublicId) {
        Place place = placeService.getEntityByPublicId(placePublicId);
        Saved saved = savedRepo.findByUserAndPlace(user, place)
                .orElseGet(() -> savedRepo.save(Saved.builder()
                        .user(user)
                        .place(place)
                        .build()));
        return placeMapper.toDto(saved.getPlace());
    }

    public void unsave(User user, UUID placePublicId) {
        Place place = placeService.getEntityByPublicId(placePublicId);
        Optional<Saved> saved = savedRepo.findByUserAndPlace(user, place);
        saved.ifPresent(savedRepo::delete);
    }

    public List<PlaceDto> getMySavedPlaces(User user) {
        return savedRepo.findAllByUser(user).stream()
                .map(saved -> placeMapper.toDto(saved.getPlace()))
                .toList();
    }
}
