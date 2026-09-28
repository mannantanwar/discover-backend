package com.discover.backend.tasteprofile;

import com.discover.backend.common.ResourceNotFoundException;
import com.discover.backend.dish.DishRepository;
import com.discover.backend.user.User;
import com.discover.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TasteProfileService {

    private final TasteProfileRepository tasteProfileRepository;
    private final TasteProfileMapper tasteProfileMapper;
    private final DishRepository dishRepository;
    private final UserService userService;

    public List<String> getAvailableTags() {
        return dishRepository.findDistinctTasteTags();
    }

    public TasteProfileDto getMyProfile(User user) {
        TasteProfile profile = tasteProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Taste profile not found for user: " + user.getPublicId()));
        return tasteProfileMapper.toDto(profile);
    }

    public TasteProfileDto submitPicks(User user, List<String> tags) {
        TasteProfile profile = tasteProfileRepository.findByUser(user)
                .orElseGet(() -> TasteProfile.builder().publicId(UUID.randomUUID()).user(user).build());
        profile.setPreferredTags(tags);
        TasteProfile saved = tasteProfileRepository.save(profile);

        // submitting real picks unambiguously means onboarding happened — no separate call needed for this path
        userService.markOnboardingSeen(user);

        return tasteProfileMapper.toDto(saved);
    }
}
