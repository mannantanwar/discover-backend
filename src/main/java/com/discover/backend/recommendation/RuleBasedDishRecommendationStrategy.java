package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import com.discover.backend.dishreview.DishReview;
import com.discover.backend.dishreview.DishReviewService;
import com.discover.backend.dishreview.DishStatsDto;
import com.discover.backend.recommendation.DishRecommendationStrategy.RankedDish;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RuleBasedDishRecommendationStrategy implements  DishRecommendationStrategy {
    private final DishReviewService dishReviewService;
    @Override
    public List<RankedDish> getRecommendedDishes(List<Dish>candidateDishes, User user){
        // ab is user ke pass hai history if rating and us rating me se iska pref vector bana pdega and then cpmare the dihses ,jiska jitna zyda overlapp utna high score // for the new user users ki popularity ke bsis pr hoga ye select

        // getting  all of the dishes made by the user to make its preferencevector
        List<DishReview> dishesReviewedByUser = dishReviewService.getAllReviewsByUser(user);
        // ab we have got all of the dishes that the user has reviewed
        List<DishReview> filteredDishes = filterDishesByRating(dishesReviewedByUser,4);
        // moved the cold-start check to here instead of right after fetching reviews —
        // a user could have reviews but none of them 4+, which is still cold start for our purposes
        if(filteredDishes.isEmpty()){
            // cold start ke andar ky ahoga basically get all the dhies and then recommed the top rated wuth the most views
            return handleColdStart(candidateDishes);
        }
        Map<String , Integer> priorityTags = new HashMap<>();
        filteredDishes.stream().
                forEach(dishReview -> {
                    Dish dish = dishReview.getDish();
                    if (dish.getTasteTags() == null) {
                        return;
                    }
                    dish.getTasteTags().stream()
                            .forEach(tasteTag -> {
                                if(priorityTags.containsKey(tasteTag)){
                                    priorityTags.put(tasteTag,priorityTags.get(tasteTag)+1);
                                }
                                else{
                                    priorityTags.put(tasteTag,1);
                                }
                            });

                });
        // now the map has been made and it is time to make a priority queue and then start the process in it
        // now I have to put the list of the conaditates in the pq and then op them on the basis of their priority or i can sort them no need of the pq ??
        // -> resolved: no PQ needed. we already have the full candidate list upfront and want ALL of it
        // back, ranked — a PQ only pays off when you're pulling top-K out of a much bigger/streaming set.
        // sorting once does the exact same job with less code.

        List<RankedDish> finalDishes= candidateDishes.stream()
                .map(d -> Map.entry(d, matchedTags(d, priorityTags)))
                .sorted(Comparator.comparingInt((Map.Entry<Dish, List<String>> e) -> calculateScore(e.getValue(), priorityTags)).reversed())
                .map(e -> buildRankedDish(e.getKey(), e.getValue()))
                .toList();
        return finalDishes;
    }

    private List<RankedDish> handleColdStart(List<Dish> candidateDishes) {
        // no rated dishes to learn from yet — rank by popularity instead of tag overlap
        return candidateDishes.stream()
                .sorted(Comparator.comparingDouble(this::popularityScore).reversed())
                .map(dish -> {
                    RankedDish rd = new RankedDish();
                    rd.setDish(dish);
                    rd.setReason("Popular choice");
                    return rd;
                })
                .toList();
    }

    private double popularityScore(Dish dish) {
        DishStatsDto stats = dishReviewService.getStatsForDish(dish);
        double avg = stats.getAverageRating() == null ? 0.0 : stats.getAverageRating();
        return avg * stats.getReviewCount();
    }

    // pulls out which of this dish's own tags actually matched the user's liked-tags map —
    // computed once, then reused for both the score and the reason message, instead of
    // filtering dish.getTasteTags() separately in each place
    private List<String> matchedTags(Dish dish, Map<String, Integer> priorityTags) {
        if (dish.getTasteTags() == null) {
            return List.of();
        }
        return dish.getTasteTags().stream()
                .filter(priorityTags::containsKey)
                .toList();
    }

    private RankedDish buildRankedDish(Dish d, List<String> matchedTags) {
        RankedDish rd  = new RankedDish();
        rd.setDish(d);
        rd.setReason(matchedTags.isEmpty()
                ? "You might like this"
                : "Because you liked " + String.join(", ", matchedTags) + " dishes");
        return rd;
    }

    private int calculateScore(List<String> matchedTags, Map<String, Integer> priorityTags) {
        return matchedTags.stream().mapToInt(priorityTags::get).sum();
    }

    private List<DishReview> filterDishesByRating(List<DishReview> dishesReviewedByUser, int rating){
        // now we can use the streams on the list
        return dishesReviewedByUser.stream()
                .filter(x->x.getRating()>=rating)
                .toList();
    }

}
