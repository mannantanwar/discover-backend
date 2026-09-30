package com.discover.backend.homefeed;

import com.discover.backend.context.ContextInput;
import com.discover.backend.context.ContextService;
import com.discover.backend.context.ContextSuggestion;
import com.discover.backend.dish.Dish;
import com.discover.backend.dish.DishService;
import com.discover.backend.recommendation.DishRecommendationService;
import com.discover.backend.recommendation.RecommendationType;
import com.discover.backend.recommendation.RecommendedDishDto;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeFeedService {

    // ST_DWithin on geography works in meters
    private static final double RADIUS_METERS = 5000.0;
    private static final int TOP_RECOMMENDED = 10;
    private static final int TOP_PER_CONTEXT = 5;

    private final DishService dishService;
    private final DishRecommendationService dishRecommendationService;
    private final ContextService contextService;

    public HomeFeedDto getHomeFeed(User user, Double latitude, Double longitude) {
        // now for this latitude and longitude we have to find the dishes at places within a certain distance from the user
        // local variable, not a field — this service is one shared instance across every request/user
        List<Dish> nearByDishes = dishService.getDishEntitiesNear(latitude, longitude, RADIUS_METERS);

        HomeFeedDto feed = new HomeFeedDto();
        feed.setRecommendedDishes(getNearByRecommendationDishes(user, nearByDishes));
        feed.setContextSections(getSmartDishRecommendation(user, nearByDishes, latitude, longitude));
        return feed;
    }

    private List<RecommendedDishDto> getNearByRecommendationDishes(User user, List<Dish> nearByDishes) {
        return dishRecommendationService
                .getRecommendedDishes(user, nearByDishes, RecommendationType.TASTE_PROFILE)
                .stream()
                .limit(TOP_RECOMMENDED)
                .toList();
    }

    // now the method that will give the recommended dish on the basis of the tags of the dish which we will be getting from the context Suggestion
    private List<HomeFeedSection> getSmartDishRecommendation(User user, List<Dish> nearByDishes, Double lat, Double lng) {
        ContextInput input = new ContextInput();
        input.setLatitude(lat);
        input.setLongitude(lng);
        input.setNow(LocalDateTime.now());
        List<ContextSuggestion> suggestions = contextService.getSuggestions(input);

        // now that we have the tags we have to make the dishes from these tags
        // we already have the list of nearby dishes — filter it by each suggestion's tags, then the same
        // taste-profile strategy ranks that shortlist (user weights from preferred tags + rated dishes, rest stays the same)
        return suggestions.stream()
                .map(suggestion -> {
                    HomeFeedSection section = new HomeFeedSection();
                    section.setMessage(suggestion.getMessage());
                    section.setDishes(dishRecommendationService
                            .getRecommendedDishesMatchingTags(user, nearByDishes,
                                    suggestion.getSuggestedTags(), RecommendationType.TASTE_PROFILE)
                            .stream()
                            .limit(TOP_PER_CONTEXT)
                            .toList());
                    return section;
                })
                // a context message with no real dish behind it is just a vague banner — drop it
                .filter(section -> !section.getDishes().isEmpty())
                .toList();
    }
}
