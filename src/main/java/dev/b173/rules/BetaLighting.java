package dev.b173.rules;

import dev.b173.core.BetaSky;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public final class BetaLighting {
    private static final float[] BRIGHTNESS = new float[16];

    static {
        float floor = 0.05F;
        for (int i = 0; i <= 15; i++) {
            float dark = 1.0F - (float) i / 15.0F;
            BRIGHTNESS[i] = (1.0F - dark) / (dark * 3.0F + 1.0F) * (1.0F - floor) + floor;
        }
    }

    private static int darkeningLevel;
    private static long darkeningTime = Long.MIN_VALUE;
    private static int darkening;

    private BetaLighting() {
    }

    public static int skyDarkening(Level level) {
        long time = level.getGameTime();
        int id = System.identityHashCode(level);
        if (id != darkeningLevel || time != darkeningTime) {
            darkening = BetaSky.skyDarkening(level.getOverworldClockTime(), 1.0F, level.getRainLevel(1.0F),
                    level.getThunderLevel(1.0F));
            darkeningLevel = id;
            darkeningTime = time;
        }
        return darkening;
    }

    public static int lightValue(Level level, BlockPos pos) {
        int sky = level.getBrightness(LightLayer.SKY, pos) - skyDarkening(level);
        int block = level.getBrightness(LightLayer.BLOCK, pos);
        return Math.max(0, Math.max(sky, block));
    }

    public static float brightness(Level level, BlockPos pos) {
        return BRIGHTNESS[lightValue(level, pos)];
    }

    public static float entityBrightness(Entity entity) {
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() * 0.66D, entity.getZ());
        return brightness(entity.level(), pos);
    }
}
