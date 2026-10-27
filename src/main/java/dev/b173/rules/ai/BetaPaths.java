package dev.b173.rules.ai;

import dev.b173.core.BetaPathFinder;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class BetaPaths {
    private BetaPaths() {
    }

    public static Path toEntity(Mob mob, Entity target, float distance) {
        return find(mob, target.getX(), target.getBoundingBox().minY, target.getZ(), distance, (int) (distance + 16.0F));
    }

    public static Path toEntityUnbounded(Mob mob, Entity target, float distance) {
        return find(mob, target.getX(), target.getBoundingBox().minY, target.getZ(), distance, -1);
    }

    public static Path toSpot(Mob mob, int x, int y, int z, float distance) {
        return find(mob, (double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F),
                distance, (int) (distance + 8.0F));
    }

    private static Path find(Mob mob, double x, double y, double z, float distance, int pad) {
        ServerLevel level = (ServerLevel) mob.level();
        int cx = Mth.floor(mob.getX());
        int cz = Mth.floor(mob.getZ());
        if (pad < 0) {
            pad = (int) distance + 16;
        }
        int minChunkX = cx - pad >> 4;
        int minChunkZ = cz - pad >> 4;
        int maxChunkX = cx + pad >> 4;
        int maxChunkZ = cz + pad >> 4;
        LevelChunk[][] chunks = new LevelChunk[maxChunkX - minChunkX + 1][maxChunkZ - minChunkZ + 1];
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                chunks[i][j] = level.getChunkSource().getChunkNow(minChunkX + i, minChunkZ + j);
            }
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BetaPathFinder.World world = new BetaPathFinder.World() {
            private int packed(int bx, int by, int bz) {
                if (by < 0 || by >= 128) {
                    return 0;
                }
                int i = (bx >> 4) - minChunkX;
                int j = (bz >> 4) - minChunkZ;
                if (i < 0 || i >= chunks.length || j < 0 || j >= chunks[i].length || chunks[i][j] == null) {
                    return 0;
                }
                return BetaBlockStates.beta(chunks[i][j].getBlockState(pos.set(bx, by, bz)));
            }

            @Override
            public int id(int bx, int by, int bz) {
                return BetaBlockStates.id(packed(bx, by, bz));
            }

            @Override
            public int meta(int bx, int by, int bz) {
                return BetaBlockStates.meta(packed(bx, by, bz));
            }
        };
        AABB box = mob.getBoundingBox();
        int[][] points = new BetaPathFinder(world).find(box.minX, box.minY, box.minZ, mob.getBbWidth(), mob.getBbHeight(),
                x, y, z, distance);
        if (points == null) {
            return null;
        }
        List<Node> nodes = new ArrayList<>(points.length);
        for (int[] p : points) {
            nodes.add(new Node(p[0], p[1], p[2]));
        }
        int[] last = points[points.length - 1];
        return new Path(nodes, new BlockPos(last[0], last[1], last[2]), true);
    }
}
