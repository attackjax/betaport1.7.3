package dev.b173.rules;

import dev.b173.core.BetaBlocks;
import dev.b173.world.BetaBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class BetaTeleporter {
    private static final int SEARCH = 128;
    private static final int BUILD = 16;
    private static final int TOP = 127;

    private BetaTeleporter() {
    }

    public static TeleportTransition destination(ServerLevel level, Entity entity) {
        boolean toNether = level.dimension() != Level.NETHER;
        ServerLevel target = level.getServer().getLevel(toNether ? Level.NETHER : Level.OVERWORLD);
        if (target == null) {
            return null;
        }
        double scale = toNether ? 1.0D / 8.0D : 8.0D;
        Vec3 arrival = new Vec3(entity.getX() * scale, entity.getY(), entity.getZ() * scale);
        Vec3 placed = find(target, arrival);
        if (placed == null) {
            build(target, arrival, target.getRandom());
            placed = find(target, arrival);
        }
        if (placed == null) {
            placed = arrival;
        }
        return new TeleportTransition(target, placed, Vec3.ZERO, entity.getYRot(), 0.0F,
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
    }

    private static boolean portal(ServerLevel level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).is(Blocks.NETHER_PORTAL);
    }

    public static Vec3 find(ServerLevel level, Vec3 at) {
        BlockPos centre = BlockPos.containing(at.x, 0, at.z);
        PoiManager poi = level.getPoiManager();
        poi.ensureLoadedAndValid(level, centre, SEARCH);
        int cx = Mth.floor(at.x);
        int cz = Mth.floor(at.z);
        List<BlockPos> bottoms = poi.getInSquare(type -> type.is(PoiTypes.NETHER_PORTAL), centre, SEARCH,
                        PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(p -> Math.abs(p.getX() - cx) <= SEARCH && Math.abs(p.getZ() - cz) <= SEARCH)
                .filter(p -> p.getY() >= 0 && p.getY() <= TOP)
                .filter(p -> level.getBlockState(p).is(Blocks.NETHER_PORTAL) && !portal(level, p.getX(), p.getY() - 1, p.getZ()))
                .sorted(Comparator.<BlockPos>comparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ)
                        .thenComparing(Comparator.comparingInt((BlockPos p) -> -p.getY())))
                .toList();
        BlockPos best = null;
        double bestDistance = -1.0D;
        for (BlockPos p : bottoms) {
            double dx = p.getX() + 0.5D - at.x;
            double dy = p.getY() + 0.5D - at.y;
            double dz = p.getZ() + 0.5D - at.z;
            double distance = dx * dx + dy * dy + dz * dz;
            if (bestDistance < 0.0D || distance < bestDistance) {
                bestDistance = distance;
                best = p;
            }
        }
        if (best == null) {
            return null;
        }
        int x = best.getX();
        int y = best.getY();
        int z = best.getZ();
        double px = x + 0.5D;
        double pz = z + 0.5D;
        if (portal(level, x - 1, y, z)) {
            px -= 0.5D;
        }
        if (portal(level, x + 1, y, z)) {
            px += 0.5D;
        }
        if (portal(level, x, y, z - 1)) {
            pz -= 0.5D;
        }
        if (portal(level, x, y, z + 1)) {
            pz += 0.5D;
        }
        return new Vec3(px, y + 0.5D, pz);
    }

    private static boolean air(ServerLevel level, int x, int y, int z) {
        return BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(new BlockPos(x, y, z)))) == 0;
    }

    private static boolean solid(ServerLevel level, int x, int y, int z) {
        return BetaBlocks.isSolid(BetaBlockStates.id(BetaBlockStates.beta(level.getBlockState(new BlockPos(x, y, z)))));
    }

    public static void build(ServerLevel level, Vec3 at, RandomSource random) {
        double best = -1.0D;
        int px = Mth.floor(at.x);
        int py = Mth.floor(at.y);
        int pz = Mth.floor(at.z);
        int bx = px;
        int by = py;
        int bz = pz;
        int facing = 0;
        int start = random.nextInt(4);

        for (int x = px - BUILD; x <= px + BUILD; x++) {
            double dx = x + 0.5D - at.x;
            for (int z = pz - BUILD; z <= pz + BUILD; z++) {
                double dz = z + 0.5D - at.z;
                for (int y = TOP; y >= 0; y--) {
                    if (!air(level, x, y, z)) {
                        continue;
                    }
                    while (y > 0 && air(level, x, y - 1, z)) {
                        y--;
                    }
                    for (int d = start; d < start + 4; d++) {
                        int a = d % 2;
                        int b = 1 - a;
                        if (d % 4 >= 2) {
                            a = -a;
                            b = -b;
                        }
                        if (!fits(level, x, y, z, a, b, 3)) {
                            continue;
                        }
                        double dy = y + 0.5D - at.y;
                        double distance = dx * dx + dy * dy + dz * dz;
                        if (best < 0.0D || distance < best) {
                            best = distance;
                            bx = x;
                            by = y;
                            bz = z;
                            facing = d % 4;
                        }
                    }
                }
            }
        }
        if (best < 0.0D) {
            for (int x = px - BUILD; x <= px + BUILD; x++) {
                double dx = x + 0.5D - at.x;
                for (int z = pz - BUILD; z <= pz + BUILD; z++) {
                    double dz = z + 0.5D - at.z;
                    for (int y = TOP; y >= 0; y--) {
                        if (!air(level, x, y, z)) {
                            continue;
                        }
                        while (y > level.getMinY() && air(level, x, y - 1, z)) {
                            y--;
                        }
                        for (int d = start; d < start + 2; d++) {
                            int a = d % 2;
                            int b = 1 - a;
                            if (!fits(level, x, y, z, a, b, 1)) {
                                continue;
                            }
                            double dy = y + 0.5D - at.y;
                            double distance = dx * dx + dy * dy + dz * dz;
                            if (best < 0.0D || distance < best) {
                                best = distance;
                                bx = x;
                                by = y;
                                bz = z;
                                facing = d % 2;
                            }
                        }
                    }
                }
            }
        }

        int a = facing % 2;
        int b = 1 - a;
        if (facing % 4 >= 2) {
            a = -a;
            b = -b;
        }
        if (best < 0.0D) {
            by = Mth.clamp(by, 70, 118);
            for (int i = -1; i <= 1; i++) {
                for (int j = 1; j < 3; j++) {
                    for (int k = -1; k < 3; k++) {
                        BlockPos p = new BlockPos(bx + (j - 1) * a + i * b, by + k, bz + (j - 1) * b - i * a);
                        level.setBlock(p, k < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                                Block.UPDATE_ALL);
                    }
                }
            }
        }
        BlockState portal = Blocks.NETHER_PORTAL.defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, a != 0 ? Direction.Axis.X : Direction.Axis.Z);
        for (int pass = 0; pass < 4; pass++) {
            for (int j = 0; j < 4; j++) {
                for (int k = -1; k < 4; k++) {
                    BlockPos p = new BlockPos(bx + (j - 1) * a, by + k, bz + (j - 1) * b);
                    boolean frame = j == 0 || j == 3 || k == -1 || k == 3;
                    level.setBlock(p, frame ? Blocks.OBSIDIAN.defaultBlockState() : portal,
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
            for (int j = 0; j < 4; j++) {
                for (int k = -1; k < 4; k++) {
                    BlockPos p = new BlockPos(bx + (j - 1) * a, by + k, bz + (j - 1) * b);
                    level.updateNeighborsAt(p, level.getBlockState(p).getBlock());
                }
            }
        }
    }

    private static boolean fits(ServerLevel level, int x, int y, int z, int a, int b, int depth) {
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = -1; k < 4; k++) {
                    int xx = x + (j - 1) * a + i * b;
                    int yy = y + k;
                    int zz = z + (j - 1) * b - i * a;
                    if (k < 0 && !solid(level, xx, yy, zz)) {
                        return false;
                    }
                    if (k >= 0 && !air(level, xx, yy, zz)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
