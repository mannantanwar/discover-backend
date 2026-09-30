package com.discover.backend.homefeed;

import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home-feed")
public class HomeFeedController {

    private final HomeFeedService homeFeedService;

    @GetMapping
    public HomeFeedDto getHomeFeed(@AuthenticationPrincipal User user,
                                   @RequestParam Double lat,
                                   @RequestParam Double lng) {
        return homeFeedService.getHomeFeed(user, lat, lng);
    }
}
