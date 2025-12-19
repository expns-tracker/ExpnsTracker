package org.expns_tracker.ExpnsTracker.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
