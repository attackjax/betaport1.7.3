package dev.b173.world;

import dev.b173.Beta173Mod;
import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaChunk;
import dev.b173.core.BetaChunkSource;
import dev.b173.core.BetaHell;
import dev.b173.core.world.BetaWorld;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Map;

final class RegionBetaWorld extends BetaWorld {
    private final WorldGenLevel level;
    private final BetaChunkSource terrain;
    private final int centreX;
    private final int centreZ;
    private final ChunkAccess[] chunks = new ChunkAccess[9];
    private final Long2IntOpenHashMap overlay = new Long2IntOpenHashMap();
    private final Long2IntOpenHashMap heights = new Long2IntOpenHashMap();
    private final Long2ObjectOpenHashMap<BetaChunk> outside = new Long2ObjectOpenHashMap<>();
    private final LongOpenHashSet leaves = new LongOpenHashSet();
    private final LongOpenHashSet snow = new LongOpenHashSet();
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private int droppedWrites;

    RegionBetaWorld(WorldGenLevel level, BetaChunkSource terrain, int centreX, int centreZ) {
        super(Rules.FAITHFUL, terrain.seed() ^ ((long) centreX * 341873128712L + (long) centreZ * 132897987541L),
                terrain instanceof BetaHell);
        keepLightForThisPassOnly();
        this.level = level;
        this.terrain = terrain;
        this.centreX = centreX;
        this.centreZ = centreZ;
        overlay.defaultReturnValue(-1);
        heights.defaultReturnValue(-1);
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return Math.abs(chunkX - centreX) <= 1 && Math.abs(chunkZ - centreZ) <= 1;
    }

    @Override
    protected boolean deferTicksOutsideLoadedArea() {
        return true;
    }

    @Override
    protected boolean tickAreaLoaded(int x, int y, int z) {
        return chunkExists(x >> 4, z >> 4);
    }

    private ChunkAccess chunk(int chunkX, int chunkZ) {
        int slot = (chunkX - centreX + 1) * 3 + (chunkZ - centreZ + 1);
        ChunkAccess chunk = chunks[slot];
        if (chunk == null) {
            chunk = level.getChunk(chunkX, chunkZ);
            chunks[slot] = chunk;
        }
        return chunk;
    }

    private int packedAt(int x, int y, int z) {
        int packed = overlay.get(BetaWorld.pack(x, y, z));
        if (packed >= 0) {
            return packed;
        }
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (chunkExists(chunkX, chunkZ)) {
            return BetaBlockStates.beta(chunk(chunkX, chunkZ).getBlockState(cursor.set(x, y, z)));
        }
        long key = (long) chunkX << 32 ^ (chunkZ & 0xFFFFFFFFL);
        BetaChunk base = outside.get(key);
        if (base == null) {
            base = terrain.generate(chunkX, chunkZ);
            outside.put(key, base);
        }
        int index = BetaChunk.index(x & 15, y, z & 15);
        return BetaBlockStates.pack(base.blocks[index] & 255, base.metadata[index]);
    }

    @Override
    protected int storageId(int x, int y, int z) {
        return BetaBlockStates.id(packedAt(x, y, z));
    }

    @Override
    protected int storageMeta(int x, int y, int z) {
        return BetaBlockStates.meta(packedAt(x, y, z));
    }

    @Override
    protected void storageSet(int x, int y, int z, int id, int meta) {
        if (!chunkExists(x >> 4, z >> 4)) {
            droppedWrites++;
            return;
        }
        long pos = BetaWorld.pack(x, y, z);
        overlay.put(pos, BetaBlockStates.pack(id, meta));
        level.setBlock(cursor.set(x, y, z), BetaBlockStates.modern(id, meta), Block.UPDATE_CLIENTS);
        if (id == BetaBlocks.LEAVES) {
            leaves.add(pos);
        } else if (id == BetaBlocks.SNOW_LAYER) {
            snow.add(pos);
        }
        long column = columnKey(x, z);
        int height = heights.get(column);
        if (height >= 0) {
            if (BetaBlocks.lightOpacity(id) != 0) {
                if (y >= height) {
                    heights.put(column, y + 1);
                }
            } else if (y == height - 1) {
                heights.remove(column);
            }
        }
    }

    @Override
    protected int storageHeight(int x, int z) {
        long column = columnKey(x, z);
        int height = heights.get(column);
        if (height < 0) {
            height = 128;
            while (height > 0 && BetaBlocks.lightOpacity(storageId(x, height - 1, z)) == 0) {
                height--;
            }
            heights.put(column, height);
        }
        return height;
    }

    private static long columnKey(int x, int z) {
        return (long) x << 32 ^ (z & 0xFFFFFFFFL);
    }

    void finish() {
        settleLeaves();
        settleSnowyGrass();
        fillChests();
        fillSpawners();
        scheduleTicks();
        tickWindowEdge();
        if (droppedWrites > 0) {
            Beta173Mod.LOGGER.debug("Population of chunk {}, {} reached {} blocks outside its region",
                    centreX, centreZ, droppedWrites);
        }
    }

    private BlockState modernAt(long pos) {
        int x = BetaWorld.unpackX(pos);
        int z = BetaWorld.unpackZ(pos);
        return chunk(x >> 4, z >> 4).getBlockState(cursor.set(x, BetaWorld.unpackY(pos), z));
    }

    private void setModern(long pos, BlockState state) {
        level.setBlock(cursor.set(BetaWorld.unpackX(pos), BetaWorld.unpackY(pos), BetaWorld.unpackZ(pos)),
                state, Block.UPDATE_CLIENTS);
    }

    private boolean inRegion(long pos) {
        return chunkExists(BetaWorld.unpackX(pos) >> 4, BetaWorld.unpackZ(pos) >> 4);
    }

    private void settleLeaves() {
        Long2IntOpenHashMap distance = new Long2IntOpenHashMap();
        distance.defaultReturnValue(-1);
        for (long pos : leaves) {
            if (getBlockId(BetaWorld.unpackX(pos), BetaWorld.unpackY(pos), BetaWorld.unpackZ(pos)) == BetaBlocks.LEAVES) {
                distance.put(pos, LeavesBlock.DECAY_DISTANCE);
            }
        }
        for (int pass = 0; pass < LeavesBlock.DECAY_DISTANCE; pass++) {
            boolean changed = false;
            for (Long2IntMap.Entry entry : distance.long2IntEntrySet()) {
                long pos = entry.getLongKey();
                int best = entry.getIntValue();
                int x = BetaWorld.unpackX(pos);
                int y = BetaWorld.unpackY(pos);
                int z = BetaWorld.unpackZ(pos);
                for (Direction direction : Direction.values()) {
                    int nx = x + direction.getStepX();
                    int ny = y + direction.getStepY();
                    int nz = z + direction.getStepZ();
                    long neighbour = BetaWorld.pack(nx, ny, nz);
                    int known = distance.get(neighbour);
                    int candidate;
                    if (known >= 0) {
                        candidate = known + 1;
                    } else {
                        int id = getBlockId(nx, ny, nz);
                        if (id == BetaBlocks.LOG) {
                            candidate = 1;
                        } else if (id == BetaBlocks.LEAVES && ny >= 0 && ny < 128 && inRegion(neighbour)) {
                            BlockState state = modernAt(neighbour);
                            if (!state.hasProperty(LeavesBlock.DISTANCE)) {
                                continue;
                            }
                            candidate = state.getValue(LeavesBlock.DISTANCE) + 1;
                        } else {
                            continue;
                        }
                    }
                    if (candidate < best) {
                        best = candidate;
                    }
                }
                if (best < entry.getIntValue()) {
                    entry.setValue(best);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        for (Long2IntMap.Entry entry : distance.long2IntEntrySet()) {
            long pos = entry.getLongKey();
            BlockState state = modernAt(pos);
            if (!state.hasProperty(LeavesBlock.DISTANCE)) {
                continue;
            }
            int d = Math.min(entry.getIntValue(), LeavesBlock.DECAY_DISTANCE);
            setModern(pos, state.setValue(LeavesBlock.DISTANCE, d)
                    .setValue(LeavesBlock.PERSISTENT, d >= LeavesBlock.DECAY_DISTANCE));
        }
    }

    private void settleSnowyGrass() {
        for (long pos : snow) {
            int x = BetaWorld.unpackX(pos);
            int y = BetaWorld.unpackY(pos);
            int z = BetaWorld.unpackZ(pos);
            if (y < 1 || getBlockId(x, y, z) != BetaBlocks.SNOW_LAYER || getBlockId(x, y - 1, z) != BetaBlocks.GRASS) {
                continue;
            }
            long below = BetaWorld.pack(x, y - 1, z);
            BlockState grass = modernAt(below);
            if (grass.hasProperty(BlockStateProperties.SNOWY)) {
                setModern(below, grass.setValue(BlockStateProperties.SNOWY, true));
            }
        }
    }

    private void fillChests() {
        for (Map.Entry<Long, List<ChestItem>> entry : chests().entrySet()) {
            long pos = entry.getKey();
            int x = BetaWorld.unpackX(pos);
            int y = BetaWorld.unpackY(pos);
            int z = BetaWorld.unpackZ(pos);
            if (!inRegion(pos) || getBlockId(x, y, z) != BetaBlocks.CHEST) {
                continue;
            }
            boolean north = BetaBlocks.isOpaqueCube(getBlockId(x, y, z - 1));
            boolean south = BetaBlocks.isOpaqueCube(getBlockId(x, y, z + 1));
            boolean west = BetaBlocks.isOpaqueCube(getBlockId(x - 1, y, z));
            boolean east = BetaBlocks.isOpaqueCube(getBlockId(x + 1, y, z));
            Direction facing = Direction.SOUTH;
            if (north && !south) {
                facing = Direction.SOUTH;
            }
            if (south && !north) {
                facing = Direction.NORTH;
            }
            if (west && !east) {
                facing = Direction.EAST;
            }
            if (east && !west) {
                facing = Direction.WEST;
            }
            setModern(pos, Blocks.CHEST.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
            BlockEntity blockEntity = level.getBlockEntity(cursor.set(x, y, z));
            if (!(blockEntity instanceof Container container)) {
                continue;
            }
            for (ChestItem item : entry.getValue()) {
                Item type = BuiltInRegistries.ITEM.getValue(Identifier.parse(item.item()));
                container.setItem(item.slot(), new ItemStack(type, item.count()));
            }
            blockEntity.setChanged();
        }
    }

    private void fillSpawners() {
        for (Map.Entry<Long, String> entry : spawners().entrySet()) {
            long pos = entry.getKey();
            int x = BetaWorld.unpackX(pos);
            int y = BetaWorld.unpackY(pos);
            int z = BetaWorld.unpackZ(pos);
            if (!inRegion(pos) || getBlockId(x, y, z) != BetaBlocks.SPAWNER) {
                continue;
            }
            if (level.getBlockEntity(cursor.set(x, y, z)) instanceof SpawnerBlockEntity spawner) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entry.getValue()));
                spawner.setEntityId(type, RandomSource.create(pos ^ terrain.seed()));
                spawner.setChanged();
            }
        }
    }

    private void scheduleTicks() {
        LongOpenHashSet seen = new LongOpenHashSet();
        for (PendingTick tick : pendingTicks()) {
            long pos = BetaWorld.pack(tick.x(), tick.y(), tick.z());
            if (tick.y() < 0 || tick.y() >= 128 || !inRegion(pos) || !seen.add(pos)) {
                continue;
            }
            int id = getBlockId(tick.x(), tick.y(), tick.z());
            if (id != tick.blockId()) {
                continue;
            }
            BlockPos at = new BlockPos(tick.x(), tick.y(), tick.z());
            switch (id) {
                case BetaBlocks.FLOWING_WATER, BetaBlocks.WATER -> level.scheduleTick(at, Fluids.WATER, 5);
                case BetaBlocks.FLOWING_LAVA, BetaBlocks.LAVA -> level.scheduleTick(at, Fluids.LAVA, 30);
                case BetaBlocks.SAND, BetaBlocks.GRAVEL -> {
                    int below = getBlockId(tick.x(), tick.y() - 1, tick.z());
                    if (below == 0 || below == BetaBlocks.FIRE || BetaBlocks.isLiquid(below)) {
                        level.scheduleTick(at, id == BetaBlocks.SAND ? Blocks.SAND : Blocks.GRAVEL, 2);
                    }
                }
                default -> {
                }
            }
        }
    }

    private void tickWindowEdge() {
        int minX = (centreX - 1) << 4;
        int maxX = ((centreX + 1) << 4) + 15;
        int minZ = (centreZ - 1) << 4;
        int maxZ = ((centreZ + 1) << 4) + 15;
        for (Long2IntMap.Entry entry : overlay.long2IntEntrySet()) {
            int id = BetaBlockStates.id(entry.getIntValue());
            if (!BetaBlocks.isLiquid(id)) {
                continue;
            }
            long pos = entry.getLongKey();
            int x = BetaWorld.unpackX(pos);
            int z = BetaWorld.unpackZ(pos);
            if (x != minX && x != maxX && z != minZ && z != maxZ) {
                continue;
            }
            BlockPos at = new BlockPos(x, BetaWorld.unpackY(pos), z);
            level.scheduleTick(at, BetaBlocks.isWater(id) ? Fluids.WATER : Fluids.LAVA, BetaBlocks.isWater(id) ? 5 : 30);
        }
    }
}
