package dev.b173.client.visual;

import dev.b173.client.BetaClientState;
import dev.b173.client.mixin.sky.ClientLevelAccessor;
import dev.b173.core.BetaSky;
import dev.b173.core.ClimateCache;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.jspecify.annotations.Nullable;

public final class BetaAtmosphere {
    private static final float WATER_FOG_START = -2.8F;
    private static final float WATER_FOG_END = 21.8F;
    private static final float LAVA_FOG_START = -0.14F;
    private static final float LAVA_FOG_END = 1.09F;

    private static final float NETHER_FOG_RED = 0.2F;
    private static final float NETHER_FOG_GREEN_BLUE = 0.03F;
    private static final int NETHER_SKY_DARKENING = 11;

    private static final float BETA_FARTHEST = 256.0F;

    private static float previousBrightness;
    private static float brightness;

    public static final int CLOUD_RANGE_CHUNKS = 33;

    private static boolean skyActive;
    private static boolean cloudsActive;
    private static boolean cloudsBetaReach;
    private static boolean farTerrainFog;
    private static final float[] SKY = new float[3];
    private static final float[] SCRATCH = new float[4];

    private static @Nullable ClimateCache cachedClimate;
    private static int cachedX;
    private static int cachedZ;
    private static int cachedRgb;

    private BetaAtmosphere() {
    }

    static float setting(int chunks) {
        double log2 = Math.log(Math.max(chunks, 1)) / Math.log(2.0D);
        double whole = Math.rint(log2);
        if (Math.abs(log2 - whole) < 1.0E-9D) {
            log2 = whole;
        }
        return (float) Math.clamp(4.0D - log2, 0.0D, 3.0D);
    }

    static float drawnSetting(int chunks) {
        return BetaVoxy.lodDistance() > 0.0F ? 0.0F : setting(chunks);
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        Entity viewer = minecraft.getCameraEntity();
        if (level == null || viewer == null || minecraft.isPaused()) {
            return;
        }
        boolean nether = BetaClientState.isBetaNether(level);
        if (!nether && !BetaClientState.isBetaDimension(level)) {
            return;
        }
        previousBrightness = brightness;
        float near = (3.0F - drawnSetting(minecraft.options.getEffectiveRenderDistance())) / 3.0F;
        float target = lightAtViewer(level, viewer, nether) * (1.0F - near) + near;
        brightness += (target - brightness) * 0.1F;
    }

    private static float lightAtViewer(ClientLevel level, Entity viewer, boolean nether) {
        double y = viewer instanceof Player ? viewer.getEyeY() : viewer.getY();
        BlockPos pos = BlockPos.containing(viewer.getX(), y, viewer.getZ());
        int darkening = nether ? NETHER_SKY_DARKENING : BetaSky.skyDarkening(level.getOverworldClockTime(), 1.0F,
                level.getRainLevel(1.0F), level.getThunderLevel(1.0F));
        int light;
        if (pos.getY() < level.getMinY()) {
            light = 0;
        } else if (pos.getY() > level.getMaxY()) {
            light = Math.max(15 - darkening, 0);
        } else {
            light = Math.max(level.getBrightness(LightLayer.SKY, pos) - darkening,
                    level.getBrightness(LightLayer.BLOCK, pos));
            light = Math.max(light, 0);
        }
        float f = 1.0F - (float) light / 15.0F;
        float floor = nether ? 0.1F : 0.05F;
        return (1.0F - f) / (f * 3.0F + 1.0F) * (1.0F - floor) + floor;
    }

    public static void fog(FogData fog, Camera camera, int renderDistance, float partialTick, ClientLevel level) {
        farTerrainFog = false;
        boolean nether = BetaClientState.isBetaNether(level);
        if (!nether && !BetaClientState.isBetaDimension(level)) {
            return;
        }
        Entity viewer = camera.entity();
        if (viewer == null || viewer instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
            return;
        }
        FogType type = camera.getFluidInCamera();
        float r;
        float g;
        float b;
        float cloudFar;
        if (type == FogType.WATER) {
            r = 0.02F;
            g = 0.02F;
            b = 0.2F;
            fog.environmentalStart = WATER_FOG_START;
            fog.environmentalEnd = WATER_FOG_END;
            fog.skyEnd = WATER_FOG_END;
            cloudFar = WATER_FOG_END;
        } else if (type == FogType.LAVA) {
            r = 0.6F;
            g = 0.1F;
            b = 0.0F;
            fog.environmentalStart = LAVA_FOG_START;
            fog.environmentalEnd = LAVA_FOG_END;
            fog.skyEnd = LAVA_FOG_END;
            cloudFar = LAVA_FOG_END;
        } else if (nether && (type == FogType.NONE || type == FogType.ATMOSPHERIC)) {
            float farTerrain = BetaVoxy.lodDistance();
            float toSky = 1.0F / (4.0F - (farTerrain > 0.0F ? 0.0F : setting(renderDistance)));
            toSky = 1.0F - (float) Math.pow((double) toSky, 0.25D);
            r = NETHER_FOG_RED * (1.0F - toSky);
            g = NETHER_FOG_GREEN_BLUE * (1.0F - toSky);
            b = NETHER_FOG_GREEN_BLUE * (1.0F - toSky);
            float drawn = Math.max((float) renderDistance * 16.0F, farTerrain);
            cloudFar = 0.0F;
            if (!Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog()) {
                fog.environmentalStart = 0.0F;
                fog.environmentalEnd = drawn;
                fog.skyEnd = drawn;
                farTerrainFog = farTerrain > 0.0F;
            }
        } else if (type == FogType.NONE || type == FogType.ATMOSPHERIC) {
            float farTerrain = BetaVoxy.lodDistance();
            float toSky = 1.0F / (4.0F - (farTerrain > 0.0F ? 0.0F : setting(renderDistance)));
            toSky = 1.0F - (float) Math.pow((double) toSky, 0.25D);
            float angle = BetaSky.celestialAngle(level.getOverworldClockTime(), partialTick);
            skyColor(level, viewer, angle, partialTick, SCRATCH);
            float skyR = SCRATCH[0];
            float skyG = SCRATCH[1];
            float skyB = SCRATCH[2];
            BetaSky.fogColor(angle, SCRATCH);
            r = SCRATCH[0] + (skyR - SCRATCH[0]) * toSky;
            g = SCRATCH[1] + (skyG - SCRATCH[1]) * toSky;
            b = SCRATCH[2] + (skyB - SCRATCH[2]) * toSky;
            float rain = level.getRainLevel(partialTick);
            if (rain > 0.0F) {
                float redGreen = 1.0F - rain * 0.5F;
                float blue = 1.0F - rain * 0.4F;
                r *= redGreen;
                g *= redGreen;
                b *= blue;
            }
            float thunder = level.getThunderLevel(partialTick);
            if (thunder > 0.0F) {
                float dim = 1.0F - thunder * 0.5F;
                r *= dim;
                g *= dim;
                b *= dim;
            }
            float chunks = (float) renderDistance * 16.0F;
            float drawn = Math.max(chunks, farTerrain);
            float betaFar = farTerrain > 0.0F ? BETA_FARTHEST : Math.min(chunks, BETA_FARTHEST);
            cloudFar = farTerrain > 0.0F ? 0.0F : betaFar;
            if (!Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog()) {
                fog.environmentalStart = drawn * 0.25F;
                fog.environmentalEnd = drawn;
                fog.skyEnd = betaFar * 0.8F;
                farTerrainFog = farTerrain > 0.0F;
            }
        } else {
            return;
        }
        float light = previousBrightness + (brightness - previousBrightness) * partialTick;
        fog.color.set(r * light, g * light, b * light, 1.0F);
        if (BetaPack.betaClouds() && cloudFar > 0.0F) {
            fog.cloudEnd = -cloudFar;
        }
    }

    public static boolean farTerrainFog() {
        return farTerrainFog;
    }

    public static void clouds(LevelRenderState state, float partialTick, @Nullable ClientLevel level) {
        cloudsActive = false;
        cloudsBetaReach = false;
        if (level == null || !BetaClientState.isBetaDimension(level) || ARGB.alpha(state.cloudColor) == 0) {
            return;
        }
        float angle = BetaSky.celestialAngle(level.getOverworldClockTime(), partialTick);
        BetaSky.cloudColor(angle, level.getRainLevel(partialTick), level.getThunderLevel(partialTick), SCRATCH);
        state.cloudColor = ARGB.colorFromFloat(0.8F, SCRATCH[0], SCRATCH[1], SCRATCH[2]);
        cloudsActive = true;
        cloudsBetaReach = BetaVoxy.lodDistance() <= 0.0F;
    }

    public static void beginClouds() {
        cloudsActive = false;
        cloudsBetaReach = false;
    }

    public static boolean cloudsActive() {
        return cloudsActive;
    }

    public static boolean cloudsBetaReach() {
        return cloudsBetaReach;
    }

    public static boolean cloudCellDrawn(boolean fancy, int dx, int dz) {
        if (fancy) {
            return dx >= -16 && dx < 32 && dz >= -16 && dz < 32;
        }
        return dx >= -21 && dx <= 21 && dz >= -21 && dz <= 21;
    }

    public static void beginSky() {
        skyActive = false;
    }

    public static void sky(ClientLevel level, float partialTick, Camera camera, SkyRenderState state) {
        if (state.skybox != DimensionType.Skybox.OVERWORLD || !BetaClientState.isBetaDimension(level)) {
            return;
        }
        Entity viewer = camera.entity();
        if (viewer == null) {
            return;
        }
        if (Minecraft.getInstance().options.getEffectiveRenderDistance() <= 4 && BetaVoxy.lodDistance() <= 0.0F) {
            state.skybox = DimensionType.Skybox.NONE;
            return;
        }
        float angle = BetaSky.celestialAngle(level.getOverworldClockTime(), partialTick);
        float rain = level.getRainLevel(partialTick);
        skyColor(level, viewer, angle, partialTick, SKY);
        state.skyColor = ARGB.colorFromFloat(1.0F, SKY[0], SKY[1], SKY[2]);
        state.sunAngle = angle * ((float) Math.PI * 2.0F);
        state.moonAngle = state.sunAngle + (float) Math.PI;
        state.starAngle = state.sunAngle;
        state.rainBrightness = 1.0F - rain;
        state.starBrightness = BetaSky.starBrightness(angle) * (1.0F - rain);
        state.sunriseAndSunsetColor = BetaSky.sunriseColor(angle, SCRATCH)
                ? ARGB.colorFromFloat(SCRATCH[3], SCRATCH[0], SCRATCH[1], SCRATCH[2]) : 0;
        state.moonPhase = MoonPhase.FULL_MOON;
        state.shouldRenderDarkDisc = true;
        skyActive = true;
    }

    public static boolean skyActive() {
        return skyActive;
    }

    public static float[] skyColor() {
        return SKY;
    }

    private static void skyColor(ClientLevel level, Entity viewer, float angle, float partialTick, float[] out) {
        int rgb = skyRgb(Mth.floor(viewer.getX()), Mth.floor(viewer.getZ()));
        int flash = ((ClientLevelAccessor) level).b173$skyFlashTime();
        BetaSky.skyColor(rgb, angle, level.getRainLevel(partialTick), level.getThunderLevel(partialTick), flash,
                partialTick, out);
    }

    private static int skyRgb(int x, int z) {
        ClimateCache climate = BetaClientState.climate();
        if (climate == null) {
            return BetaSky.skyColorByTemperature(0.0F);
        }
        if (climate != cachedClimate || x != cachedX || z != cachedZ) {
            cachedClimate = climate;
            cachedX = x;
            cachedZ = z;
            cachedRgb = BetaSky.skyColorByTemperature((float) climate.skyTemperature(x, z));
        }
        return cachedRgb;
    }
}
