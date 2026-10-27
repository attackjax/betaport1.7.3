package dev.b173.rules;

import dev.b173.core.BetaBlocks;
import dev.b173.world.BetaBlockStates;
import dev.b173.world.BetaChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class BetaSpawnPoint {
    private static final int MAX_STEPS = 100_000;

    private BetaSpawnPoint() {
    }

    public static boolean applies(ServerLevel level) {
        return level.dimension() == Level.OVERWORLD && level.getChunkSource().getGenerator() instanceof BetaChunkGenerator;
    }

    static int topBlock(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 63, z);
        while (!level.getBlockState(pos.setY(pos.getY() + 1)).isAir()) {
        }
        pos.setY(pos.getY() - 1);
        return BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(pos)));
    }

    public static boolean setInitial(ServerLevel level, ServerLevelData data) {
        Random random = new Random();
        int x = 0;
        int z = 0;
        for (int steps = 0; topBlock(level, x, z) != BetaBlocks.SAND; steps++) {
            if (steps >= MAX_STEPS) {
                return false;
            }
            x += random.nextInt(64) - random.nextInt(64);
            z += random.nextInt(64) - random.nextInt(64);
        }
        data.setSpawn(LevelData.RespawnData.of(level.dimension(), new BlockPos(x, 64, z), 0.0F, 0.0F));
        return true;
    }

    public static Vec3 place(ServerLevel level, BlockPos spawn) {
        int x = spawn.getX();
        int z = spawn.getZ();
        if (topBlock(level, x, z) == 0) {
            Random random = new Random();
            while (topBlock(level, x, z) == 0) {
                x += random.nextInt(8) - random.nextInt(8);
                z += random.nextInt(8) - random.nextInt(8);
            }
            level.setRespawnData(LevelData.RespawnData.of(level.dimension(), new BlockPos(x, spawn.getY(), z), 0.0F, 0.0F));
        }
        double px = x + 0.5D;
        double pz = z + 0.5D;
        double y = spawn.getY() + 1;
        AABB box = EntityType.PLAYER.getDimensions().makeBoundingBox(px, y, pz);
        while (y < level.getMaxY() && !level.noCollision(box)) {
            y += 1.0D;
            box = box.move(0.0D, 1.0D, 0.0D);
        }
        return new Vec3(px, y, pz);
    }
}
