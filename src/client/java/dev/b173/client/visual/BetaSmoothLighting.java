package dev.b173.client.visual;

import dev.b173.client.BetaClientState;
import dev.b173.core.BetaSky;
import dev.b173.core.BetaSmoothLight;
import dev.b173.world.BetaBlockStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public final class BetaSmoothLighting {
    public static final int FULL_LIGHT = 0xF000F0;

    private static final float[] OVERWORLD = BetaSmoothLight.table(0.05F);
    private static final float[] NETHER = BetaSmoothLight.table(0.1F);
    private static final Direction[] FACES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
            Direction.WEST, Direction.EAST};

    private static volatile int darkening;
    private static volatile boolean active;
    private static volatile boolean betaWorld;
    private static volatile boolean nether;
    private static int built = -1;

    private BetaSmoothLighting() {
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        betaWorld = level != null && BetaPack.betaLight() && BetaClientState.onBetaServer()
                && (level.dimension() == Level.NETHER || BetaClientState.isBetaDimension(level));
        boolean on = betaWorld && minecraft.options.ambientOcclusion().get();
        if (!on) {
            active = false;
            built = -1;
            return;
        }
        nether = level.dimension() == Level.NETHER;
        darkening = nether ? 0 : BetaSky.skyDarkening(level.getOverworldClockTime(), 1.0F,
                level.getRainLevel(1.0F), level.getThunderLevel(1.0F));
        if (!active) {
            active = true;
            built = darkening;
            minecraft.levelRenderer.allChanged();
            return;
        }
        if (darkening != built) {
            built = darkening;
            rebuildAll(minecraft);
        }
    }

    private static void rebuildAll(Minecraft minecraft) {
        BlockPos at = minecraft.gameRenderer.getMainCamera().blockPosition();
        int radius = minecraft.options.getEffectiveRenderDistance() + 1;
        int cx = at.getX() >> 4;
        int cz = at.getZ() >> 4;
        int minY = minecraft.level.getMinSectionY();
        int maxY = minecraft.level.getMaxSectionY();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = minY; y <= maxY; y++) {
                    minecraft.levelRenderer.setSectionDirty(x, y, z);
                }
            }
        }
    }

    public static boolean applies(BlockState state) {
        return active && BetaSmoothLight.SMOOTH[BetaBlockStates.id(BetaBlockStates.beta(state))];
    }

    public static boolean flat(BlockState state) {
        return active && !BetaSmoothLight.SMOOTH[BetaBlockStates.id(BetaBlockStates.beta(state))];
    }

    public enum Flat {
        SAME,
        FULL,
        SIDES_FACING,
        TOP_ABOVE,
        UNSHADED
    }

    public static Flat flatRule(BlockState state) {
        if (!betaWorld) {
            return Flat.SAME;
        }
        return switch (BetaBlockStates.id(BetaBlockStates.beta(state))) {
            case 50, 76 -> Flat.FULL;
            case 81 -> Flat.SIDES_FACING;
            case 26 -> Flat.TOP_ABOVE;
            case 27, 28, 66 -> Flat.UNSHADED;
            default -> Flat.SAME;
        };
    }

    public static int face(Direction direction) {
        return switch (direction) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
    }

    public static float[] corners(BlockAndTintGetter level, BlockPos pos, BlockState state, int face) {
        int lightValue = BetaSmoothLight.LIGHT_VALUE[BetaBlockStates.id(BetaBlockStates.beta(state))];
        Access access = ACCESS.get();
        access.reset(level);
        return BetaSmoothLight.face(access, BetaSmoothLight.GRASS, face, pos.getX(), pos.getY(), pos.getZ(), lightValue);
    }

    private static final ThreadLocal<Access> ACCESS = ThreadLocal.withInitial(Access::new);

    private static final class Access implements BetaSmoothLight.Access {
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        private BlockAndTintGetter level;
        private float[] table;
        private int dark;

        void reset(BlockAndTintGetter level) {
            this.level = level;
            this.table = nether ? NETHER : OVERWORLD;
            this.dark = darkening;
        }

        @Override
        public int id(int x, int y, int z) {
            return BetaBlockStates.id(BetaBlockStates.beta(this.level.getBlockState(this.pos.set(x, y, z))));
        }

        @Override
        public float brightness(int x, int y, int z, int minLight) {
            return this.table[Math.max(level(x, y, z, true), minLight)];
        }

        private int level(int x, int y, int z, boolean neighbours) {
            if (neighbours && BetaSmoothLight.takesNeighbourLight(id(x, y, z))) {
                int best = level(x, y + 1, z, false);
                best = Math.max(best, level(x + 1, y, z, false));
                best = Math.max(best, level(x - 1, y, z, false));
                best = Math.max(best, level(x, y, z + 1, false));
                best = Math.max(best, level(x, y, z - 1, false));
                return best;
            }
            if (y < 0) {
                return 0;
            }
            this.pos.set(x, Math.min(y, 127), z);
            int sky = this.level.getBrightness(LightLayer.SKY, this.pos) - this.dark;
            int block = this.level.getBrightness(LightLayer.BLOCK, this.pos);
            return Math.max(sky, block);
        }
    }

    static Direction direction(int face) {
        return FACES[face];
    }
}
