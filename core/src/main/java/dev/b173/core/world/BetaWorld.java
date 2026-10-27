package dev.b173.core.world;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BetaWorld {
    public record Rules(boolean blockCallbacks, boolean immediateTicks, LightModel light, boolean overwriteSameId) {
        public static final Rules FAITHFUL = new Rules(true, true, LightModel.COLUMN, false);
        public static final Rules ISOLATED = new Rules(false, false, LightModel.BINARY, true);
    }

    public enum LightModel {
        BINARY,
        COLUMN
    }

    public record PendingTick(int x, int y, int z, int blockId, int delay) {
    }

    public record ChestItem(int slot, String item, int count, int damage) {
    }

    protected final Rules rules;
    private final boolean hellWorld;
    private final BetaRandom worldRandom;
    private final List<PendingTick> pendingTicks = new ArrayList<>();
    private final Map<Long, List<ChestItem>> chests = new HashMap<>();
    private final Map<Long, String> spawners = new HashMap<>();
    private final BetaFluids water = new BetaFluids(false);
    private final BetaFluids lava = new BetaFluids(true);
    private Map<Long, byte[]> passLight;

    public boolean scheduledUpdatesAreImmediate;
    public boolean editingBlocks;
    public boolean fallInstantly;
    private int tickDepth;

    protected BetaWorld(Rules rules, long worldRandomSeed) {
        this(rules, worldRandomSeed, false);
    }

    protected BetaWorld(Rules rules, long worldRandomSeed, boolean hellWorld) {
        this.rules = rules;
        this.hellWorld = hellWorld;
        this.worldRandom = new BetaRandom(worldRandomSeed);
    }

    public Rules rules() {
        return rules;
    }

    public final boolean isHellWorld() {
        return hellWorld;
    }

    protected abstract int storageId(int x, int y, int z);

    protected abstract int storageMeta(int x, int y, int z);

    protected abstract void storageSet(int x, int y, int z, int id, int meta);

    protected abstract int storageHeight(int x, int z);

    protected int storedSkyLight(int x, int y, int z) {
        return passLight != null ? passColumn(x, z)[y] : columnSkyLight(x, y, z);
    }

    protected void storeSkyLight(int x, int y, int z, int light) {
        if (passLight != null) {
            passColumn(x, z)[y] = (byte) light;
        }
    }

    protected void beforeColumnChange(int x, int z) {
        if (passLight != null) {
            passColumn(x, z);
        }
    }

    protected final void keepLightForThisPassOnly() {
        passLight = new HashMap<>();
    }

    private byte[] passColumn(int x, int z) {
        Long key = (long) x << 32 ^ (z & 0xFFFFFFFFL);
        byte[] column = passLight.get(key);
        if (column == null) {
            column = new byte[128];
            int height = storageHeight(x, z);
            for (int y = height; y < 128; y++) {
                column[y] = 15;
            }
            int light = 15;
            int y = height;
            while (y > 0 && light > 0) {
                y--;
                light = Math.max(light - Math.max(BetaBlocks.lightOpacity(storageId(x, y, z)), 1), 0);
                column[y] = (byte) light;
            }
            passLight.put(key, column);
        }
        return column;
    }

    protected final int columnSkyLight(int x, int y, int z) {
        int height = storageHeight(x, z);
        if (y >= height) {
            return 15;
        }
        int light = 15;
        for (int yy = height - 1; yy >= y; yy--) {
            light -= Math.max(BetaBlocks.lightOpacity(storageId(x, yy, z)), 1);
            if (light <= 0) {
                return 0;
            }
        }
        return light;
    }

    public abstract boolean chunkExists(int chunkX, int chunkZ);

    public final int getBlockId(int x, int y, int z) {
        if (y < 0 || y >= 128) {
            return 0;
        }
        return storageId(x, y, z);
    }

    public final int getBlockMetadata(int x, int y, int z) {
        if (y < 0 || y >= 128) {
            return 0;
        }
        return storageMeta(x, y, z);
    }

    public final boolean isAirBlock(int x, int y, int z) {
        return getBlockId(x, y, z) == 0;
    }

    public final boolean isSolid(int x, int y, int z) {
        return BetaBlocks.isSolid(getBlockId(x, y, z));
    }

    public final boolean isLiquid(int x, int y, int z) {
        return BetaBlocks.isLiquid(getBlockId(x, y, z));
    }

    public final boolean isWater(int x, int y, int z) {
        return BetaBlocks.isWater(getBlockId(x, y, z));
    }

    public final int getHeightValue(int x, int z) {
        return storageHeight(x, z);
    }

    public final boolean canBlockSeeTheSky(int x, int y, int z) {
        return y >= storageHeight(x, z);
    }

    public final int skyLight(int x, int y, int z) {
        if (y < 0) {
            return 0;
        }
        if (y >= 128) {
            y = 127;
        }
        if (rules.light() == LightModel.BINARY) {
            return y >= storageHeight(x, z) ? 15 : 0;
        }
        return storedSkyLight(x, y, z);
    }

    public final int getFullBlockLightValue(int x, int y, int z) {
        return skyLight(x, y, z);
    }

    public final int findTopSolidBlock(int x, int z) {
        for (int y = 127; y > 0; y--) {
            int id = storageId(x, y, z);
            if (BetaBlocks.isSolid(id) || BetaBlocks.isLiquid(id)) {
                return y + 1;
            }
        }
        return -1;
    }

    public final boolean checkChunksExist(int x0, int y0, int z0, int x1, int y1, int z1) {
        if (y1 < 0 || y0 >= 128) {
            return false;
        }
        int cx0 = x0 >> 4;
        int cz0 = z0 >> 4;
        int cx1 = x1 >> 4;
        int cz1 = z1 >> 4;
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                if (!chunkExists(cx, cz)) {
                    return false;
                }
            }
        }
        return true;
    }

    public final boolean setBlock(int x, int y, int z, int id) {
        if (y < 0 || y >= 128) {
            return false;
        }
        int old = storageId(x, y, z);
        if (old == id && !rules.overwriteSameId()) {
            return false;
        }
        int oldHeight = beforeChange(x, z);
        storageSet(x, y, z, id, 0);
        if (old != 0 && rules.blockCallbacks()) {
            onBlockRemoval(x, y, z, old);
        }
        relightAfterChange(x, y, z, id, oldHeight);
        if (id != 0 && rules.blockCallbacks()) {
            onBlockAdded(x, y, z, id);
        }
        return true;
    }

    public final boolean setBlockAndMetadata(int x, int y, int z, int id, int meta) {
        if (y < 0 || y >= 128) {
            return false;
        }
        int old = storageId(x, y, z);
        if (old == id && storageMeta(x, y, z) == meta && !rules.overwriteSameId()) {
            return false;
        }
        int oldHeight = beforeChange(x, z);
        storageSet(x, y, z, id, meta);
        if (old != 0 && rules.blockCallbacks()) {
            onBlockRemoval(x, y, z, old);
        }
        relightAfterChange(x, y, z, id, oldHeight);
        if (id != 0 && rules.blockCallbacks()) {
            onBlockAdded(x, y, z, id);
        }
        return true;
    }

    private int beforeChange(int x, int z) {
        if (rules.light() != LightModel.COLUMN) {
            return 0;
        }
        beforeColumnChange(x, z);
        return storageHeight(x, z);
    }

    private void relightAfterChange(int x, int y, int z, int id, int oldHeight) {
        if (rules.light() != LightModel.COLUMN) {
            return;
        }
        if (BetaBlocks.lightOpacity(id) != 0) {
            if (y >= oldHeight) {
                relightBlock(x, y + 1, z, oldHeight);
            }
        } else if (y == oldHeight - 1) {
            relightBlock(x, y, z, oldHeight);
        }
    }

    private void relightBlock(int x, int y, int z, int oldHeight) {
        int top = Math.max(oldHeight, y);
        while (top > 0 && BetaBlocks.lightOpacity(storageId(x, top - 1, z)) == 0) {
            top--;
        }
        if (top == oldHeight) {
            return;
        }
        if (top < oldHeight) {
            for (int yy = top; yy < oldHeight; yy++) {
                storeSkyLight(x, yy, z, 15);
            }
        } else {
            for (int yy = oldHeight; yy < top; yy++) {
                storeSkyLight(x, yy, z, 0);
            }
        }
        int light = 15;
        int yy = top;
        while (yy > 0 && light > 0) {
            yy--;
            light = Math.max(light - Math.max(BetaBlocks.lightOpacity(storageId(x, yy, z)), 1), 0);
            storeSkyLight(x, yy, z, light);
        }
    }

    public final boolean setBlockMetadata(int x, int y, int z, int meta) {
        if (y < 0 || y >= 128) {
            return false;
        }
        storageSet(x, y, z, storageId(x, y, z), meta);
        return true;
    }

    public final boolean setBlockWithNotify(int x, int y, int z, int id) {
        if (setBlock(x, y, z, id)) {
            notifyBlocksOfNeighborChange(x, y, z, id);
            return true;
        }
        return false;
    }

    public final boolean setBlockAndMetadataWithNotify(int x, int y, int z, int id, int meta) {
        if (setBlockAndMetadata(x, y, z, id, meta)) {
            notifyBlocksOfNeighborChange(x, y, z, id);
            return true;
        }
        return false;
    }

    public final void setBlockMetadataWithNotify(int x, int y, int z, int meta) {
        if (setBlockMetadata(x, y, z, meta)) {
            notifyBlocksOfNeighborChange(x, y, z, getBlockId(x, y, z));
        }
    }

    public final void notifyBlocksOfNeighborChange(int x, int y, int z, int id) {
        notifyBlockOfNeighborChange(x - 1, y, z, id);
        notifyBlockOfNeighborChange(x + 1, y, z, id);
        notifyBlockOfNeighborChange(x, y - 1, z, id);
        notifyBlockOfNeighborChange(x, y + 1, z, id);
        notifyBlockOfNeighborChange(x, y, z - 1, id);
        notifyBlockOfNeighborChange(x, y, z + 1, id);
    }

    private void notifyBlockOfNeighborChange(int x, int y, int z, int changedId) {
        if (editingBlocks || !rules.blockCallbacks()) {
            return;
        }
        int id = getBlockId(x, y, z);
        if (id != 0) {
            onNeighborBlockChange(x, y, z, id);
        }
    }

    public final void scheduleBlockUpdate(int x, int y, int z, int id, int delay) {
        if (!tickAreaLoaded(x, y, z)) {
            if (deferTicksOutsideLoadedArea()) {
                pendingTicks.add(new PendingTick(x, y, z, id, delay));
            }
            return;
        }
        if (scheduledUpdatesAreImmediate) {
            if (!rules.immediateTicks()) {
                return;
            }
            int current = getBlockId(x, y, z);
            if (current == id && current > 0) {
                if (tickDepth > MAX_TICK_DEPTH) {
                    pendingTicks.add(new PendingTick(x, y, z, id, delay));
                    return;
                }
                tickDepth++;
                try {
                    updateTick(x, y, z, id, worldRandom);
                } finally {
                    tickDepth--;
                }
            }
            return;
        }
        pendingTicks.add(new PendingTick(x, y, z, id, delay));
    }

    private static final int MAX_TICK_DEPTH = 384;

    protected boolean tickAreaLoaded(int x, int y, int z) {
        return checkChunksExist(x - 8, y - 8, z - 8, x + 8, y + 8, z + 8);
    }

    protected boolean deferTicksOutsideLoadedArea() {
        return false;
    }

    public final void updateTick(int x, int y, int z, int id, BetaRandom random) {
        switch (id) {
            case BetaBlocks.FLOWING_WATER -> water.updateTick(this, x, y, z, random);
            case BetaBlocks.FLOWING_LAVA -> lava.updateTick(this, x, y, z, random);
            case BetaBlocks.SAND, BetaBlocks.GRAVEL -> tryToFall(x, y, z, id);
            default -> {
            }
        }
    }

    public List<PendingTick> pendingTicks() {
        return pendingTicks;
    }

    private void onBlockRemoval(int x, int y, int z, int old) {
        switch (old) {
            case BetaBlocks.LEAVES -> markLeavesForDecay(x, y, z, 1, false);
            case BetaBlocks.LOG -> markLeavesForDecay(x, y, z, 4, true);
            case BetaBlocks.CHEST -> chests.remove(pack(x, y, z));
            case BetaBlocks.SPAWNER -> spawners.remove(pack(x, y, z));
            default -> {
            }
        }
    }

    private void markLeavesForDecay(int x, int y, int z, int radius, boolean onlyUnmarked) {
        int reach = radius + 1;
        if (!checkChunksExist(x - reach, y - reach, z - reach, x + reach, y + reach, z + reach)) {
            return;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if ((dx | dy | dz) == 0 || getBlockId(x + dx, y + dy, z + dz) != BetaBlocks.LEAVES) {
                        continue;
                    }
                    int meta = getBlockMetadata(x + dx, y + dy, z + dz);
                    if (!onlyUnmarked || (meta & 8) == 0) {
                        setBlockMetadata(x + dx, y + dy, z + dz, meta | 8);
                    }
                }
            }
        }
    }

    final void lavaMixEffects() {
        worldRandom.nextFloat();
        worldRandom.nextFloat();
    }

    final void dropBlockAsItem(int id) {
        int quantity = id == BetaBlocks.SNOW_LAYER ? 0 : 1;
        for (int i = 0; i < quantity; i++) {
            worldRandom.nextFloat();
            boolean drops = switch (id) {
                case BetaBlocks.TALL_GRASS -> worldRandom.nextInt(8) == 0;
                case BetaBlocks.DEAD_BUSH -> false;
                default -> true;
            };
            if (drops) {
                worldRandom.nextFloat();
                worldRandom.nextFloat();
                worldRandom.nextFloat();
            }
        }
    }

    private void onBlockAdded(int x, int y, int z, int id) {
        switch (id) {
            case BetaBlocks.FLOWING_WATER -> water.onFlowingAdded(this, x, y, z);
            case BetaBlocks.FLOWING_LAVA -> lava.onFlowingAdded(this, x, y, z);
            case BetaBlocks.WATER -> water.checkForHarden(this, x, y, z);
            case BetaBlocks.LAVA -> lava.checkForHarden(this, x, y, z);
            case BetaBlocks.SAND, BetaBlocks.GRAVEL -> scheduleBlockUpdate(x, y, z, id, 3);
            default -> {
            }
        }
    }

    private void onNeighborBlockChange(int x, int y, int z, int id) {
        switch (id) {
            case BetaBlocks.FLOWING_WATER -> water.checkForHarden(this, x, y, z);
            case BetaBlocks.FLOWING_LAVA -> lava.checkForHarden(this, x, y, z);
            case BetaBlocks.WATER -> water.onStillNeighborChange(this, x, y, z);
            case BetaBlocks.LAVA -> lava.onStillNeighborChange(this, x, y, z);
            case BetaBlocks.SAND, BetaBlocks.GRAVEL -> scheduleBlockUpdate(x, y, z, id, 3);
            case BetaBlocks.SAPLING, BetaBlocks.TALL_GRASS, BetaBlocks.DEAD_BUSH, BetaBlocks.DANDELION,
                 BetaBlocks.ROSE, BetaBlocks.BROWN_MUSHROOM, BetaBlocks.RED_MUSHROOM -> {
                if (!BetaPlants.canBlockStay(this, id, x, y, z)) {
                    dropBlockAsItem(id);
                    setBlockWithNotify(x, y, z, 0);
                }
            }
            case BetaBlocks.REEDS -> {
                if (!BetaPlants.reedCanStay(this, x, y, z)) {
                    dropBlockAsItem(id);
                    setBlockWithNotify(x, y, z, 0);
                }
            }
            case BetaBlocks.CACTUS -> {
                if (!BetaPlants.cactusCanStay(this, x, y, z)) {
                    dropBlockAsItem(id);
                    setBlockWithNotify(x, y, z, 0);
                }
            }
            case BetaBlocks.SNOW_LAYER -> {
                if (!BetaPlants.snowCanStay(this, x, y, z)) {
                    dropBlockAsItem(id);
                    setBlockWithNotify(x, y, z, 0);
                }
            }
            default -> {
            }
        }
    }

    private void tryToFall(int x, int y, int z, int id) {
        if (!canFallBelow(x, y - 1, z) || y < 0) {
            return;
        }
        if (fallInstantly || !checkChunksExist(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32)) {
            setBlockWithNotify(x, y, z, 0);
            while (canFallBelow(x, y - 1, z) && y > 0) {
                y--;
            }
            if (y > 0) {
                setBlockWithNotify(x, y, z, id);
            }
        } else {
            pendingTicks.add(new PendingTick(x, y, z, id, 3));
        }
    }

    private boolean canFallBelow(int x, int y, int z) {
        int id = getBlockId(x, y, z);
        return id == 0 || id == BetaBlocks.FIRE || BetaBlocks.isLiquid(id);
    }

    public final void addChestItem(int x, int y, int z, ChestItem item) {
        chests.computeIfAbsent(pack(x, y, z), k -> new ArrayList<>()).add(item);
    }

    public final void setSpawner(int x, int y, int z, String entity) {
        spawners.put(pack(x, y, z), entity);
    }

    public Map<Long, List<ChestItem>> chests() {
        return chests;
    }

    public Map<Long, String> spawners() {
        return spawners;
    }

    public static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | (long) y & 0xFFFL;
    }

    public static int unpackX(long packed) {
        return (int) (packed >> 38);
    }

    public static int unpackY(long packed) {
        return (int) (packed << 52 >> 52);
    }

    public static int unpackZ(long packed) {
        return (int) (packed << 26 >> 38);
    }
}
