package com.discover.backend.tasteprofile;

import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/taste-profile")
public class TasteProfileController {

    private final TasteProfileService tasteProfileService;

    @GetMapping("/tags")
    public List<String> getAvailableTags() {
        return tasteProfileService.getAvailableTags();
    }

    @GetMapping
    public TasteProfileDto getMyProfile(@AuthenticationPrincipal User user) {
        return tasteProfileService.getMyProfile(user);
    }

    @PostMapping
    public TasteProfileDto submitPicks(@AuthenticationPrincipal User user, @RequestBody TasteProfileRequest request) {
        return tasteProfileService.submitPicks(user, request.getTags());
    }
}
