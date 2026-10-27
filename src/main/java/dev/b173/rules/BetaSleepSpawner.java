package dev.b173.rules;

import dev.b173.rules.ai.BetaPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.List;

public final class BetaSleepSpawner {
    private static final EntityType<?>[] NIGHT_MOBS = {EntityType.SPIDER, EntityType.ZOMBIE, EntityType.SKELETON};
    private static final float PATH_RANGE = 32.0F;
    private static final double SLEEPER_POS_Y = 0.9375D - 0.6875D;

    private BetaSleepSpawner() {
    }

    public static boolean wakeWithMonster(ServerLevel level, List<ServerPlayer> players) {
        boolean woke = false;
        RandomSource random = level.getRandom();
        BetaSpawner.Spot spot = new BetaSpawner.Spot(level);
        for (ServerPlayer player : List.copyOf(players)) {
            if (!player.isSleeping() || player.isSpectator()) {
                continue;
            }
            boolean spawned = false;
            for (int i = 0; i < 20 && !spawned; i++) {
                int x = Mth.floor(player.getX()) + random.nextInt(32) - random.nextInt(32);
                int z = Mth.floor(player.getZ()) + random.nextInt(32) - random.nextInt(32);
                int y = Mth.floor(player.getY()) + random.nextInt(16) - random.nextInt(16);
                if (y < 1) {
                    y = 1;
                } else if (y > 128) {
                    y = 128;
                }
                EntityType<?> type = NIGHT_MOBS[random.nextInt(NIGHT_MOBS.length)];
                int spawnY = y;
                while (spawnY > 2 && !solidBelow(spot, x, spawnY, z)) {
                    spawnY--;
                }
                while (!spot.groundSuits(false, x, spawnY, z) && spawnY < y + 16 && spawnY < 128) {
                    spawnY++;
                }
                if (spawnY >= y + 16 || spawnY >= 128) {
                    continue;
                }
                float fx = (float) x + 0.5F;
                float fy = (float) spawnY;
                float fz = (float) z + 0.5F;
                float yaw = random.nextFloat() * 360.0F;
                if (!spot.canSpawnHere(type, MobCategory.MONSTER, fx, fy, fz, x, spawnY, z, 0)
                        || !reachesBed(level, type, fx, fy, fz, yaw, player)) {
                    continue;
                }
                BlockPos beside = besideBed(level, player);
                if (beside == null) {
                    beside = new BlockPos(x, spawnY + 1, z);
                }
                Mob monster = BetaSpawner.spawn(spot, type, (float) beside.getX() + 0.5F, (float) beside.getY(),
                        (float) beside.getZ() + 0.5F, 0.0F, 0);
                if (monster != null) {
                    player.stopSleepInBed(true, true);
                    monster.playAmbientSound();
                    woke = true;
                    spawned = true;
                }
            }
        }
        return woke;
    }

    private static boolean solidBelow(BetaSpawner.Spot spot, int x, int y, int z) {
        BlockState below = spot.state(x, y - 1, z);
        return below != null && spot.normalCube(below, x, y - 1, z);
    }

    private static boolean reachesBed(ServerLevel level, EntityType<?> type, float x, float y, float z, float yaw,
                                      ServerPlayer player) {
        if (!(type.create(level, EntitySpawnReason.NATURAL) instanceof Mob mob)) {
            return false;
        }
        mob.snapTo(x, y, z, yaw, 0.0F);
        mob.setOnGround(true);
        Path path = BetaPaths.toEntityUnbounded(mob, player, PATH_RANGE);
        mob.discard();
        if (path == null || path.getNodeCount() <= 1) {
            return false;
        }
        Node end = path.getEndNode();
        return end != null && Math.abs((double) end.x - player.getX()) < 1.5D
                && Math.abs((double) end.z - player.getZ()) < 1.5D
                && Math.abs((double) end.y - (player.getY() + SLEEPER_POS_Y)) < 1.5D;
    }

    private static BlockPos besideBed(ServerLevel level, ServerPlayer player) {
        BlockPos head = player.getSleepingPos().orElse(null);
        if (head == null) {
            return null;
        }
        BlockState bed = level.getBlockState(head);
        if (!(bed.getBlock() instanceof BedBlock)) {
            return null;
        }
        return BetaBeds.emptySpot(level, head, bed.getValue(BedBlock.FACING), 1);
    }
}
