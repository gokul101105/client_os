package com.clientos.backend.health;

public enum HealthBand {
    HEALTHY("Healthy", "🟢"),          // green circle
    NEEDS_ATTENTION("Needs Attention", "🟡"), // yellow circle
    AT_RISK("At Risk", "🔴");          // red circle

    private final String label;
    private final String emoji;

    HealthBand(String label, String emoji) {
        this.label = label;
        this.emoji = emoji;
    }

    public String getLabel() {
        return label;
    }

    public String getEmoji() {
        return emoji;
    }

    public static HealthBand forScore(int score) {
        if (score >= 80) return HEALTHY;
        if (score >= 50) return NEEDS_ATTENTION;
        return AT_RISK;
    }
}
