package org.expns_tracker.ExpnsTracker.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("Standard user"),
    ADMIN("Administrator");

    private final String description;
}