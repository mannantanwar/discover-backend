package com.discover.backend.saved;

import com.discover.backend.place.PlaceDto;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/saved-places")
public class SavedController {

    private final SavedService savedService;

    @PostMapping("/{placePublicId}")
    public PlaceDto save(@AuthenticationPrincipal User user, @PathVariable UUID placePublicId) {
        return savedService.save(user, placePublicId);
    }

    @DeleteMapping("/{placePublicId}")
    public void unsave(@AuthenticationPrincipal User user, @PathVariable UUID placePublicId) {
        savedService.unsave(user, placePublicId);
    }

    @GetMapping
    public List<PlaceDto> getMySavedPlaces(@AuthenticationPrincipal User user) {
        return savedService.getMySavedPlaces(user);
    }
}
