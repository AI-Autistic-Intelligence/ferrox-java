package dev.ferrox.utils.metrics;

public class PercentageUtils {

    /**
     * Calculates the percentage change between a current value and a previous value.
     *
     * @param currentValue  The current value.
     * @param previousValue The previous value.
     * @return The percentage change as a double, or null if previousValue is zero or null.
     */
    public static Double calculatePercentageChange(Double currentValue, Double previousValue) {
        if (currentValue == null || previousValue == null) {
            return null;
        }
        if (previousValue == 0.0) {
            return currentValue > 0 ? 100.0 : (currentValue < 0 ? -100.0 : 0.0);
        }
        return ((currentValue - previousValue) / Math.abs(previousValue)) * 100.0;
    }

    /**
     * Determines the trend ("up", "down", "neutral") based on percentage change.
     */
    public static String getTrend(Double percentageChange) {
        if (percentageChange == null || percentageChange == 0.0) {
            return "neutral";
        }
        return percentageChange > 0 ? "up" : "down";
    }
}
