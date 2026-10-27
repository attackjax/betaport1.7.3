package dev.b173.world;

import dev.b173.core.world.BetaWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

public final class LiveBetaWorld extends BetaWorld {
    private static final Rules LIVE = new Rules(false, false, LightModel.BINARY, false);

    private final ServerLevel level;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    public LiveBetaWorld(ServerLevel level) {
        super(LIVE, level.getSeed());
        this.level = level;
    }

    @Override
    protected int storageId(int x, int y, int z) {
        return BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(cursor.set(x, y, z))));
    }

    @Override
    protected int storageMeta(int x, int y, int z) {
        return BetaBlockStates.meta(BetaBlockStates.beta(level.getBlockState(cursor.set(x, y, z))));
    }

    @Override
    protected void storageSet(int x, int y, int z, int id, int meta) {
        level.setBlock(cursor.set(x, y, z), BetaBlockStates.modern(id, meta), Block.UPDATE_ALL);
    }

    @Override
    protected int storageHeight(int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return level.hasChunk(chunkX, chunkZ);
    }
}
