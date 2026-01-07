package org.expns_tracker.ExpnsTracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalCategorizerTest {

    private LocalCategorizer localCategorizer;

    private static final String DEFAULT_EXPENSE_ID = "99dd6d0c05a347d1858daa219e21c573";
    private static final String DEFAULT_INCOME_ID = "e4ea4e4e11ee45df85a179996c35d798";

    private static final String FOOD_ID = "47ea44117c6543178b3fefae8ffada52";
    private static final String TRANSPORT_ID = "e005b42a480b4cad924db7cfe5300337";
    private static final String SHOPPING_ID = "26e47df594f74e7b972935105a91881e";
    private static final String INCOME_SPECIFIC_ID = "0d5ab7626aa746ffb638a862aa1a386a";

    @BeforeEach
    void setUp() {
        localCategorizer = new LocalCategorizer();
        localCategorizer.init();
    }

    @ParameterizedTest
    @CsvSource({
            "Starbucks Coffee, -5.00, " + FOOD_ID,
            "Tesco Supermarket, -50.00, " + FOOD_ID,
            "Uber Trip, -15.00, " + TRANSPORT_ID,
            "TFL Travel Charge, -2.50, " + TRANSPORT_ID,
            "Amazon Marketplace, -20.00, " + SHOPPING_ID,
            "Netflix Subscription, -10.00, a18d9bf1d24b44589726ae811717cb75", // Leisure ID
            "Rent Payment, -1000.00, f5220586cb184ec38d4b65384a40f91e" // Household ID
    })
    void categorize_KnownExpenses_ReturnsCorrectCategory(String description, Double amount, String expectedId) {
        String result = localCategorizer.categorize(description, amount);
        assertEquals(expectedId, result);
    }

    @Test
    void categorize_CaseInsensitiveMatching() {
        String result = localCategorizer.categorize("UBER TRIP", -10.0);
        assertEquals(TRANSPORT_ID, result);
    }

    @Test
    void categorize_UnknownExpense_ReturnsDefaultId() {
        String result = localCategorizer.categorize("Random Corner Shop", -5.50);
        assertEquals(DEFAULT_EXPENSE_ID, result);
    }

    @Test
    void categorize_NullDescription_ReturnsDefaultId() {
        String result = localCategorizer.categorize(null, -10.0);
        assertEquals(DEFAULT_EXPENSE_ID, result);
    }

    @Test
    void categorize_PositiveAmount_UnknownDescription_ReturnsGeneralIncomeId() {
        String result = localCategorizer.categorize("Gift from friend", 50.0);
        assertEquals(DEFAULT_INCOME_ID, result);
    }

    @Test
    void categorize_PositiveAmount_SpecificIncomeKeyword_ReturnsSpecificIncomeId() {
        String result = localCategorizer.categorize("Monthly Salary", 2500.0);
        assertEquals(INCOME_SPECIFIC_ID, result);
    }

    @Test
    void categorize_PositiveAmount_WithRefundKeyword_CategorizesAsExpenseReversal() {
        String result = localCategorizer.categorize("Refund from Amazon", 20.0);
        assertEquals(INCOME_SPECIFIC_ID, result);
    }

    @Test
    void categorize_PositiveAmount_WithRefundKeyword_UnknownVendor_ReturnsDefaultExpense() {
        String result = localCategorizer.categorize("Refund from Unknown", 20.0);
        assertEquals(INCOME_SPECIFIC_ID, result);
    }
}
