package org.expns_tracker.ExpnsTracker.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalCategorizer {

    private final Map<Pattern, String> rules = new HashMap<>();

    private static final String DEFAULT_CATEGORY_ID = "99dd6d0c05a347d1858daa219e21c573";

    private static final String DEFAULT_INCOME_ID = "e4ea4e4e11ee45df85a179996c35d798";

    @PostConstruct
    public void init() {
        loadRules();
    }

    private void loadRules() {
        // --- EXPENSES: Food & Drinks (ID: 47ea44117c6543178b3fefae8ffada52) ---
        String foodId = "47ea44117c6543178b3fefae8ffada52";
        addRule("starbucks|costa|pret|mcdonalds|burger king|kfc|subway|dominos|pizza", foodId);
        addRule("restaurant|cafe|bistro|pub|bar", foodId);
        addRule("sainsburys|tesco|asda|lidl|aldi|waitrose|morrisons|whole foods", foodId);

        // --- EXPENSES: Transport (ID: e005b42a480b4cad924db7cfe5300337) ---
        String transportId = "e005b42a480b4cad924db7cfe5300337";
        addRule("uber|lyft|bolt|taxi", transportId);
        addRule("tfl|train|rail|bus|metro|underground", transportId);
        addRule("shell|bp|texaco|esso|fuel|petrol", transportId);

        // --- EXPENSES: Shopping (ID: 26e47df594f74e7b972935105a91881e) ---
        String shoppingId = "26e47df594f74e7b972935105a91881e";
        addRule("amazon|ebay|etsy|shopify", shoppingId);
        addRule("h&m|zara|uniqlo|asos|nike|adidas", shoppingId);
        addRule("apple|samsung|currys|pc world", shoppingId);

        // --- EXPENSES: Household & Services (ID: 15be09f7fe82405da1aa498fa92121fa - Note: using main Expenses parent or household specific)
        // Correct Household ID from list: f5220586cb184ec38d4b65384a40f91e
        String householdId = "f5220586cb184ec38d4b65384a40f91e";
        addRule("vodafone|ee|o2|three|virgin media|bt|sky", householdId);
        addRule("british gas|edf|eon|npower|scottish power|bulb|octopus", householdId);
        addRule("rent|mortgage|landlord", householdId);

        // --- EXPENSES: Leisure (ID: a18d9bf1d24b44589726ae811717cb75) ---
        String leisureId = "a18d9bf1d24b44589726ae811717cb75";
        addRule("netflix|spotify|disney|prime video|hulu|hbo", leisureId);
        addRule("steam|playstation|xbox|nintendo", leisureId);
        addRule("cinema|theater|concert|ticketmaster", leisureId);

        // --- INCOME: Income (ID: 0d5ab7626aa746ffb638a862aa1a386a) ---
        String incomeId = "0d5ab7626aa746ffb638a862aa1a386a";
        addRule("salary|wages|dividend", incomeId);
        addRule("deposit|refund", incomeId);
    }

    private void addRule(String regex, String categoryId) {
        rules.put(Pattern.compile("(?i).*" + regex + ".*"), categoryId);
    }

    /**
     * Returns the Category ID for the given transaction description.
     */
    public String categorize(String description, Double amount) {
        if (description == null) return DEFAULT_CATEGORY_ID;

        if (amount != null && amount > 0 && !description.toLowerCase().contains("refund")) {
            for (Map.Entry<Pattern, String> entry : rules.entrySet()) {
                if (entry.getKey().matcher(description).matches()) {
                    return entry.getValue();
                }
            }
            return DEFAULT_INCOME_ID;
        }

        for (Map.Entry<Pattern, String> entry : rules.entrySet()) {
            if (entry.getKey().matcher(description).matches()) {
                return entry.getValue();
            }
        }

        return DEFAULT_CATEGORY_ID;
    }
}