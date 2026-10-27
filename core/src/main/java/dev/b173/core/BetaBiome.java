package dev.b173.core;

public enum BetaBiome {
    RAINFOREST("rainforest", "Rainforest", 0x08FA36, true, false),
    SWAMPLAND("swampland", "Swampland", 0x07F9B2, true, false),
    SEASONAL_FOREST("seasonal_forest", "Seasonal Forest", 0x9BE023, true, false),
    FOREST("forest", "Forest", 0x056621, true, false),
    SAVANNA("savanna", "Savanna", 0xD9E023, true, false),
    SHRUBLAND("shrubland", "Shrubland", 0xA1AD20, true, false),
    TAIGA("taiga", "Taiga", 0x2EB153, true, true),
    DESERT("desert", "Desert", 0xFA9418, false, false),
    PLAINS("plains", "Plains", 0xFFD910, true, false),
    ICE_DESERT("ice_desert", "Ice Desert", 0xFFED93, false, true),
    TUNDRA("tundra", "Tundra", 0x57EBF9, true, true);

    private static final BetaBiome[] LOOKUP = new BetaBiome[64 * 64];
    private static final BetaBiome[] VALUES = values();

    static {
        for (int t = 0; t < 64; t++) {
            for (int h = 0; h < 64; h++) {
                LOOKUP[t + h * 64] = classify((float) t / 63.0F, (float) h / 63.0F);
            }
        }
    }

    private final String id;
    private final String displayName;
    private final int mapColor;
    private final boolean rains;
    private final boolean snows;

    BetaBiome(String id, String displayName, int mapColor, boolean rains, boolean snows) {
        this.id = id;
        this.displayName = displayName;
        this.mapColor = mapColor;
        this.rains = rains;
        this.snows = snows;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int mapColor() {
        return mapColor;
    }

    public boolean rains() {
        return rains;
    }

    public boolean snows() {
        return snows;
    }

    public int topBlock() {
        return this == DESERT || this == ICE_DESERT ? BetaBlocks.SAND : BetaBlocks.GRASS;
    }

    public int fillerBlock() {
        return this == DESERT || this == ICE_DESERT ? BetaBlocks.SAND : BetaBlocks.DIRT;
    }

    public static BetaBiome byOrdinal(int ordinal) {
        return VALUES[ordinal];
    }

    public static BetaBiome fromClimate(double temperature, double humidity) {
        int t = (int) (temperature * 63.0D);
        int h = (int) (humidity * 63.0D);
        return LOOKUP[t + h * 64];
    }

    private static BetaBiome classify(float temperature, float humidity) {
        humidity *= temperature;
        if (temperature < 0.1F) {
            return TUNDRA;
        }
        if (humidity < 0.2F) {
            if (temperature < 0.5F) {
                return TUNDRA;
            }
            return temperature < 0.95F ? SAVANNA : DESERT;
        }
        if (humidity > 0.5F && temperature < 0.7F) {
            return SWAMPLAND;
        }
        if (temperature < 0.5F) {
            return TAIGA;
        }
        if (temperature < 0.97F) {
            return humidity < 0.35F ? SHRUBLAND : FOREST;
        }
        if (humidity < 0.45F) {
            return PLAINS;
        }
        return humidity < 0.9F ? SEASONAL_FOREST : RAINFOREST;
    }
}
