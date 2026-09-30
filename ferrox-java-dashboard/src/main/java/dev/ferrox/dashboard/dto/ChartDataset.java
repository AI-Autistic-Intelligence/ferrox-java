package dev.ferrox.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataset {
    private String label;
    private List<Number> data;
    private String backgroundColor;
    private String borderColor;
    private boolean fill;
}
