package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import com.discover.backend.dishreview.DishReviewService;
import com.discover.backend.recommendation.DishRecommendationStrategy.RankedDish;
import com.discover.backend.tasteprofile.TasteProfileService;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TasteProfileDishRecommendationStrategy implements DishRecommendationStrategy {

    private final TasteProfileService tasteProfileService;
    private final DishReviewService dishReviewService;
    private final PopularityFallbackRanker popularityFallbackRanker;

    @Override
    public List<RankedDish> getRecommendedDishes(List<Dish> candidateDishes, User user) {
        // tp basically isme krna kya hai hi we have to build the user vector and the dish vector and then find the similarity between them,
        // cosine similarity

        // ab user weight kaha se build kru
        // we can get the tags from 2 ways
        //1) user ne login ke time seed kre honge preferred tags
        //2) user ke liked reviews
        // now inse i can make a map tag-> count and similarly i can make the map from the candidate dishes too like har dish ka apna tag-> count

        // building the user weights first
        Map<String, Double> userWeights = buildUserWeights(user);

        // agar dono sources se kuch nahi mila (naya user, no reviews, no picks) — cosine similarity
        // is undefined against an all-zero vector (magnitude 0, so we'd be dividing by zero), fall back to popularity
        if (userWeights.isEmpty()) {
            return popularityFallbackRanker.rankByPopularity(candidateDishes);
        }

        // now we have the user vector time for the dish vector for each candidate dish and then compare them by their cosine similarity
        return candidateDishes.stream()
                .map(dish -> Map.entry(dish, cosineSimilarity(userWeights, dish)))
                .sorted(Comparator.comparingDouble((Map.Entry<Dish, Double> e) -> e.getValue()).reversed())
                .map(x -> buildRankedDish(x.getKey(), userWeights))
                .toList();
    }

    // explicit picks and behavioral signal count equally for now (+1.0 each) — no tuned
    // weighting between them yet, nothing to justify favoring one over the other without real data
    private Map<String, Double> buildUserWeights(User user) {
        // now have to build the user weights from the 2 places as mentioned in the main function
        Map<String, Double> weights = new HashMap<>();
        // first case, get all the liked dish reviews, pull out dishes from them and then their tags
        dishReviewService.getAllReviewsByUser(user)
                .stream()
                .filter(x -> x.getRating() >= 4)
                .toList()
                .forEach(review -> {
                    List<String> tags = review.getDish().getTasteTags();
                    if (tags == null) {
                        return;
                    }
                    tags.forEach(tag -> weights.merge(tag, 1.0, Double::sum));
                });

        // now the case 2
        tasteProfileService.getPreferredTags(user).forEach(tag -> {
            weights.merge(tag, 1.0, Double::sum);
        });
        return weights;
    }

    // sparse cosine similarity — a dish's own tag list IS its vector (each present tag = weight 1),
    // no need to materialize a dense array over the full tag vocabulary since irrelevant
    // dimensions contribute nothing to the dot product or either magnitude anyway
    private double cosineSimilarity(Map<String, Double> userWeights, Dish dish) {
        List<String> dishTags = dish.getTasteTags();
        if (dishTags == null || dishTags.isEmpty()) {
            return 0.0;
        }

        // time to build the cosine similarity
        // magnitude needs the sum of SQUARES of each weight, not the sum of the weights themselves
        double userMagnitude = Math.sqrt(userWeights.values().stream()
                .mapToDouble(w -> w * w)
                .sum());

        double dishMagnitude = Math.sqrt(dishTags.size());

        double dot = dishTags
                .stream()
                .filter(userWeights::containsKey)
                .mapToDouble(userWeights::get)
                .sum();

        // dot / (userMagnitude * dishMagnitude) — needs the parentheses, otherwise / and *
        // run left-to-right and you end up multiplying by dishMagnitude instead of dividing by it
        return dot / (userMagnitude * dishMagnitude);
    }

    private RankedDish buildRankedDish(Dish dish, Map<String, Double> userWeights) {
        List<String> matchedTags = dish.getTasteTags() == null
                ? List.of()
                : dish.getTasteTags().stream().filter(userWeights::containsKey).toList();

        RankedDish rd = new RankedDish();
        rd.setDish(dish);
        rd.setReason(matchedTags.isEmpty()
                ? "You might like this"
                : "Because it matches your taste in " + String.join(", ", matchedTags));
        return rd;
    }
}
