package dev.b173.core.world;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaRandom;

final class BetaFluids {
    private final boolean lava;
    private final int flowingId;
    private final int stillId;
    private final int[] flowCost = new int[4];
    private final boolean[] optimal = new boolean[4];
    private int numAdjacentSources;

    BetaFluids(boolean lava) {
        this.lava = lava;
        this.flowingId = lava ? BetaBlocks.FLOWING_LAVA : BetaBlocks.FLOWING_WATER;
        this.stillId = lava ? BetaBlocks.LAVA : BetaBlocks.WATER;
    }

    private int tickRate() {
        return lava ? 30 : 5;
    }

    private boolean sameMaterial(int id) {
        return lava ? BetaBlocks.isLava(id) : BetaBlocks.isWater(id);
    }

    private int getFlowDecay(BetaWorld world, int x, int y, int z) {
        if (!sameMaterial(world.getBlockId(x, y, z))) {
            return -1;
        }
        return world.getBlockMetadata(x, y, z);
    }

    private int getSmallestFlowDecay(BetaWorld world, int x, int y, int z, int smallest) {
        int decay = getFlowDecay(world, x, y, z);
        if (decay < 0) {
            return smallest;
        }
        if (decay == 0) {
            numAdjacentSources++;
        }
        if (decay >= 8) {
            decay = 0;
        }
        return smallest >= 0 && decay >= smallest ? smallest : decay;
    }

    void updateTick(BetaWorld world, int x, int y, int z, BetaRandom random) {
        int level = getFlowDecay(world, x, y, z);
        int spread = lava && !world.isHellWorld() ? 2 : 1;
        boolean settle = true;
        if (level > 0) {
            int smallest = -100;
            numAdjacentSources = 0;
            smallest = getSmallestFlowDecay(world, x - 1, y, z, smallest);
            smallest = getSmallestFlowDecay(world, x + 1, y, z, smallest);
            smallest = getSmallestFlowDecay(world, x, y, z - 1, smallest);
            smallest = getSmallestFlowDecay(world, x, y, z + 1, smallest);
            int next = smallest + spread;
            if (next >= 8 || smallest < 0) {
                next = -1;
            }
            int above = getFlowDecay(world, x, y + 1, z);
            if (above >= 0) {
                next = above >= 8 ? above : above + 8;
            }
            if (numAdjacentSources >= 2 && !lava) {
                int below = world.getBlockId(x, y - 1, z);
                if (BetaBlocks.isSolid(below)) {
                    next = 0;
                } else if (sameMaterial(below) && world.getBlockMetadata(x, y, z) == 0) {
                    next = 0;
                }
            }
            if (lava && level < 8 && next < 8 && next > level && random.nextInt(4) != 0) {
                next = level;
                settle = false;
            }
            if (next != level) {
                level = next;
                if (level < 0) {
                    world.setBlockWithNotify(x, y, z, 0);
                } else {
                    world.setBlockMetadataWithNotify(x, y, z, level);
                    world.scheduleBlockUpdate(x, y, z, flowingId, tickRate());
                    world.notifyBlocksOfNeighborChange(x, y, z, flowingId);
                }
            } else if (settle) {
                toStill(world, x, y, z);
            }
        } else {
            toStill(world, x, y, z);
        }
        if (canDisplace(world, x, y - 1, z)) {
            world.setBlockAndMetadataWithNotify(x, y - 1, z, flowingId, level >= 8 ? level : level + 8);
        } else if (level >= 0 && (level == 0 || blocksFlow(world, x, y - 1, z))) {
            boolean[] directions = optimalDirections(world, x, y, z);
            int next = level >= 8 ? 1 : level + spread;
            if (next >= 8) {
                return;
            }
            if (directions[0]) {
                flowInto(world, x - 1, y, z, next);
            }
            if (directions[1]) {
                flowInto(world, x + 1, y, z, next);
            }
            if (directions[2]) {
                flowInto(world, x, y, z - 1, next);
            }
            if (directions[3]) {
                flowInto(world, x, y, z + 1, next);
            }
        }
    }

    private void toStill(BetaWorld world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockAndMetadata(x, y, z, stillId, meta);
    }

    private void flowInto(BetaWorld world, int x, int y, int z, int level) {
        if (canDisplace(world, x, y, z)) {
            int displaced = world.getBlockId(x, y, z);
            if (displaced > 0) {
                if (lava) {
                    world.lavaMixEffects();
                } else {
                    world.dropBlockAsItem(displaced);
                }
            }
            world.setBlockAndMetadataWithNotify(x, y, z, flowingId, level);
        }
    }

    private boolean[] optimalDirections(BetaWorld world, int x, int y, int z) {
        for (int d = 0; d < 4; d++) {
            flowCost[d] = 1000;
            int nx = x;
            int nz = z;
            if (d == 0) {
                nx--;
            }
            if (d == 1) {
                nx++;
            }
            if (d == 2) {
                nz--;
            }
            if (d == 3) {
                nz++;
            }
            if (blocksFlow(world, nx, y, nz)
                    || sameMaterial(world.getBlockId(nx, y, nz)) && world.getBlockMetadata(nx, y, nz) == 0) {
                continue;
            }
            if (!blocksFlow(world, nx, y - 1, nz)) {
                flowCost[d] = 0;
            } else {
                flowCost[d] = flowCost(world, nx, y, nz, 1, d);
            }
        }
        int best = flowCost[0];
        for (int d = 1; d < 4; d++) {
            if (flowCost[d] < best) {
                best = flowCost[d];
            }
        }
        for (int d = 0; d < 4; d++) {
            optimal[d] = flowCost[d] == best;
        }
        return optimal;
    }

    private int flowCost(BetaWorld world, int x, int y, int z, int depth, int from) {
        int best = 1000;
        for (int d = 0; d < 4; d++) {
            if (d == 0 && from == 1 || d == 1 && from == 0 || d == 2 && from == 3 || d == 3 && from == 2) {
                continue;
            }
            int nx = x;
            int nz = z;
            if (d == 0) {
                nx--;
            }
            if (d == 1) {
                nx++;
            }
            if (d == 2) {
                nz--;
            }
            if (d == 3) {
                nz++;
            }
            if (blocksFlow(world, nx, y, nz)
                    || sameMaterial(world.getBlockId(nx, y, nz)) && world.getBlockMetadata(nx, y, nz) == 0) {
                continue;
            }
            if (!blocksFlow(world, nx, y - 1, nz)) {
                return depth;
            }
            if (depth >= 4) {
                continue;
            }
            int cost = flowCost(world, nx, y, nz, depth + 1, d);
            if (cost < best) {
                best = cost;
            }
        }
        return best;
    }

    private boolean blocksFlow(BetaWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (id == BetaBlocks.WOODEN_DOOR || id == BetaBlocks.IRON_DOOR || id == BetaBlocks.SIGN
                || id == BetaBlocks.LADDER || id == BetaBlocks.REEDS) {
            return true;
        }
        if (id == 0) {
            return false;
        }
        return BetaBlocks.isSolid(id);
    }

    private boolean canDisplace(BetaWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (sameMaterial(id)) {
            return false;
        }
        if (BetaBlocks.isLava(id)) {
            return false;
        }
        return !blocksFlow(world, x, y, z);
    }

    void onFlowingAdded(BetaWorld world, int x, int y, int z) {
        checkForHarden(world, x, y, z);
        if (world.getBlockId(x, y, z) == flowingId) {
            world.scheduleBlockUpdate(x, y, z, flowingId, tickRate());
        }
    }

    void onStillNeighborChange(BetaWorld world, int x, int y, int z) {
        checkForHarden(world, x, y, z);
        if (world.getBlockId(x, y, z) == stillId) {
            int meta = world.getBlockMetadata(x, y, z);
            world.editingBlocks = true;
            world.setBlockAndMetadata(x, y, z, flowingId, meta);
            world.scheduleBlockUpdate(x, y, z, flowingId, tickRate());
            world.editingBlocks = false;
        }
    }

    void checkForHarden(BetaWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (id != flowingId && id != stillId) {
            return;
        }
        if (!lava) {
            return;
        }
        boolean touching = world.isWater(x, y, z - 1) || world.isWater(x, y, z + 1)
                || world.isWater(x - 1, y, z) || world.isWater(x + 1, y, z) || world.isWater(x, y + 1, z);
        if (touching) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta == 0) {
                world.setBlockWithNotify(x, y, z, BetaBlocks.OBSIDIAN);
            } else if (meta <= 4) {
                world.setBlockWithNotify(x, y, z, BetaBlocks.COBBLESTONE);
            }
            world.lavaMixEffects();
        }
    }
}
