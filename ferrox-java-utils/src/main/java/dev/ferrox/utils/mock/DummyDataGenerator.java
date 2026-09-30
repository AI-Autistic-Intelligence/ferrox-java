package dev.ferrox.utils.mock;

import dev.ferrox.dashboard.dto.ChartData;
import dev.ferrox.dashboard.dto.ChartDataset;
import dev.ferrox.dashboard.dto.MetricCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DummyDataGenerator {

    private static final Random RANDOM = new Random();

    public static List<MetricCard> generateMockMetricCards() {
        return Arrays.asList(
                MetricCard.builder()
                        .title("Total Revenue")
                        .value("$ 45,231")
                        .previousValue("$ 42,000")
                        .percentageChange(7.69)
                        .trend("up")
                        .icon("dollar-sign")
                        .build(),
                MetricCard.builder()
                        .title("Active Users")
                        .value("1,245")
                        .previousValue("1,300")
                        .percentageChange(-4.23)
                        .trend("down")
                        .icon("users")
                        .build(),
                MetricCard.builder()
                        .title("New Signups")
                        .value("320")
                        .previousValue("320")
                        .percentageChange(0.0)
                        .trend("neutral")
                        .icon("user-plus")
                        .build()
        );
    }

    public static ChartData generateMockLineChart() {
        List<String> labels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul");
        
        List<Number> data1 = new ArrayList<>();
        List<Number> data2 = new ArrayList<>();
        
        for (int i = 0; i < labels.size(); i++) {
            data1.add(RANDOM.nextInt(100) + 50);
            data2.add(RANDOM.nextInt(100) + 20);
        }

        ChartDataset dataset1 = ChartDataset.builder()
                .label("Current Year")
                .data(data1)
                .borderColor("#4f46e5")
                .backgroundColor("rgba(79, 70, 229, 0.1)")
                .fill(true)
                .build();

        ChartDataset dataset2 = ChartDataset.builder()
                .label("Previous Year")
                .data(data2)
                .borderColor("#9ca3af")
                .backgroundColor("rgba(156, 163, 175, 0.1)")
                .fill(false)
                .build();

        return ChartData.builder()
                .labels(labels)
                .datasets(Arrays.asList(dataset1, dataset2))
                .build();
    }
}
