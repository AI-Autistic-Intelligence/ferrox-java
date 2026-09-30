package dev.ferrox.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricCard {
    private String title;
    private String value;
    private String previousValue;
    private Double percentageChange;
    private String trend; // e.g., "up", "down", "neutral"
    private String icon;
}
