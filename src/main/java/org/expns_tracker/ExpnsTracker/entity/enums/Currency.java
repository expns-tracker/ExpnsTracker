package org.expns_tracker.ExpnsTracker.entity.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    EUR("Euro", "€"),
    USD("US Dollar", "$"),
    RON("Leu românesc", "lei");

    private final String label;
    private final String symbol;
}

