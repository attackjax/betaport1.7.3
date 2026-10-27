package dev.b173.core.populate;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaMath;
import dev.b173.core.BetaRandom;
import dev.b173.core.world.BetaWorld;

public final class BetaBigTree {
    private static final byte[] OTHER_AXES = {2, 0, 0, 1, 2, 1};
    private static final double HEIGHT_ATTENUATION = 0.618D;
    private static final double BRANCH_SLOPE = 0.381D;
    private static final double SCALE_WIDTH = 1.0D;
    private static final double LEAF_DENSITY = 1.0D;
    private static final int HEIGHT_LIMIT_LIMIT = 12;
    private static final int POPULATION_LEAF_DISTANCE = 5;
    private static final int SAPLING_LEAF_DISTANCE = 4;

    private final BetaWorld world;
    private final BetaRandom random = new BetaRandom(0L);
    private final int[] base = new int[3];
    private int heightLimit;
    private int height;
    private int[][] leafNodes;
    private final int leafDistanceLimit;

    private BetaBigTree(BetaWorld world, int leafDistanceLimit) {
        this.world = world;
        this.leafDistanceLimit = leafDistanceLimit;
    }

    static boolean generate(BetaWorld world, BetaRandom populationRandom, int x, int y, int z) {
        return generate(world, populationRandom, x, y, z, POPULATION_LEAF_DISTANCE);
    }

    public static boolean fromSapling(BetaWorld world, BetaRandom random, int x, int y, int z) {
        return generate(world, random, x, y, z, SAPLING_LEAF_DISTANCE);
    }

    private static boolean generate(BetaWorld world, BetaRandom populationRandom, int x, int y, int z,
                                    int leafDistanceLimit) {
        BetaBigTree tree = new BetaBigTree(world, leafDistanceLimit);
        tree.random.setSeed(populationRandom.nextLong());
        tree.base[0] = x;
        tree.base[1] = y;
        tree.base[2] = z;
        tree.heightLimit = 5 + tree.random.nextInt(HEIGHT_LIMIT_LIMIT);
        if (!tree.validTreeLocation()) {
            return false;
        }
        tree.generateLeafNodeList();
        tree.generateLeaves();
        tree.generateTrunk();
        tree.generateLeafNodeBases();
        return true;
    }

    private void generateLeafNodeList() {
        height = (int) ((double) heightLimit * HEIGHT_ATTENUATION);
        if (height >= heightLimit) {
            height = heightLimit - 1;
        }
        int perLayer = (int) (1.382D + Math.pow(LEAF_DENSITY * (double) heightLimit / 13.0D, 2.0D));
        if (perLayer < 1) {
            perLayer = 1;
        }
        int[][] nodes = new int[perLayer * heightLimit][4];
        int y = base[1] + heightLimit - leafDistanceLimit;
        int count = 1;
        int trunkTop = base[1] + height;
        int layer = y - base[1];
        nodes[0][0] = base[0];
        nodes[0][1] = y;
        nodes[0][2] = base[2];
        nodes[0][3] = trunkTop;
        y--;
        while (layer >= 0) {
            float size = layerSize(layer);
            if (size < 0.0F) {
                y--;
                layer--;
                continue;
            }
            for (int i = 0; i < perLayer; i++) {
                double distance = SCALE_WIDTH * ((double) size * ((double) random.nextFloat() + 0.328D));
                double angle = (double) random.nextFloat() * 2.0D * 3.14159D;
                int nx = BetaMath.floor(distance * StrictMath.sin(angle) + (double) base[0] + 0.5D);
                int nz = BetaMath.floor(distance * StrictMath.cos(angle) + (double) base[2] + 0.5D);
                int[] node = {nx, y, nz};
                int[] top = {nx, y + leafDistanceLimit, nz};
                if (checkBlockLine(node, top) != -1) {
                    continue;
                }
                int[] branchBase = {base[0], base[1], base[2]};
                double horizontal = Math.sqrt(Math.pow(Math.abs(base[0] - node[0]), 2.0D)
                        + Math.pow(Math.abs(base[2] - node[2]), 2.0D));
                double drop = horizontal * BRANCH_SLOPE;
                if ((double) node[1] - drop > (double) trunkTop) {
                    branchBase[1] = trunkTop;
                } else {
                    branchBase[1] = (int) ((double) node[1] - drop);
                }
                if (checkBlockLine(branchBase, node) == -1) {
                    nodes[count][0] = nx;
                    nodes[count][1] = y;
                    nodes[count][2] = nz;
                    nodes[count][3] = branchBase[1];
                    count++;
                }
            }
            y--;
            layer--;
        }
        leafNodes = new int[count][4];
        System.arraycopy(nodes, 0, leafNodes, 0, count);
    }

    private void genTreeLayer(int x, int y, int z, float radius) {
        int extent = (int) ((double) radius + 0.618D);
        for (int dx = -extent; dx <= extent; dx++) {
            for (int dz = -extent; dz <= extent; dz++) {
                double distance = Math.sqrt(Math.pow((double) Math.abs(dx) + 0.5D, 2.0D)
                        + Math.pow((double) Math.abs(dz) + 0.5D, 2.0D));
                if (distance > (double) radius) {
                    continue;
                }
                int id = world.getBlockId(x + dx, y, z + dz);
                if (id == 0 || id == BetaBlocks.LEAVES) {
                    world.setBlock(x + dx, y, z + dz, BetaBlocks.LEAVES);
                }
            }
        }
    }

    private float layerSize(int layer) {
        if ((double) layer < (double) (float) heightLimit * 0.3D) {
            return -1.618F;
        }
        float half = (float) heightLimit / 2.0F;
        float offset = (float) heightLimit / 2.0F - (float) layer;
        float size;
        if (offset == 0.0F) {
            size = half;
        } else if (Math.abs(offset) >= half) {
            size = 0.0F;
        } else {
            size = (float) Math.sqrt(Math.pow(Math.abs(half), 2.0D) - Math.pow(Math.abs(offset), 2.0D));
        }
        return size * 0.5F;
    }

    private float leafSize(int layer) {
        if (layer < 0 || layer >= leafDistanceLimit) {
            return -1.0F;
        }
        return layer != 0 && layer != leafDistanceLimit - 1 ? 3.0F : 2.0F;
    }

    private void generateLeaves() {
        for (int[] node : leafNodes) {
            for (int y = node[1]; y < node[1] + leafDistanceLimit; y++) {
                genTreeLayer(node[0], y, node[2], leafSize(y - node[1]));
            }
        }
    }

    private void placeBlockLine(int[] from, int[] to, int block) {
        int[] delta = {0, 0, 0};
        int major = 0;
        for (int axis = 0; axis < 3; axis++) {
            delta[axis] = to[axis] - from[axis];
            if (Math.abs(delta[axis]) > Math.abs(delta[major])) {
                major = axis;
            }
        }
        if (delta[major] == 0) {
            return;
        }
        int axisA = OTHER_AXES[major];
        int axisB = OTHER_AXES[major + 3];
        int direction = delta[major] > 0 ? 1 : -1;
        double slopeA = (double) delta[axisA] / (double) delta[major];
        double slopeB = (double) delta[axisB] / (double) delta[major];
        int[] point = {0, 0, 0};
        for (int i = 0, end = delta[major] + direction; i != end; i += direction) {
            point[major] = BetaMath.floor((double) (from[major] + i) + 0.5D);
            point[axisA] = BetaMath.floor((double) from[axisA] + (double) i * slopeA + 0.5D);
            point[axisB] = BetaMath.floor((double) from[axisB] + (double) i * slopeB + 0.5D);
            world.setBlock(point[0], point[1], point[2], block);
        }
    }

    private void generateTrunk() {
        placeBlockLine(new int[]{base[0], base[1], base[2]},
                new int[]{base[0], base[1] + height, base[2]}, BetaBlocks.LOG);
    }

    private void generateLeafNodeBases() {
        int[] from = {base[0], base[1], base[2]};
        for (int[] node : leafNodes) {
            int[] to = {node[0], node[1], node[2]};
            from[1] = node[3];
            if ((double) (from[1] - base[1]) >= (double) heightLimit * 0.2D) {
                placeBlockLine(from, to, BetaBlocks.LOG);
            }
        }
    }

    private int checkBlockLine(int[] from, int[] to) {
        int[] delta = {0, 0, 0};
        int major = 0;
        for (int axis = 0; axis < 3; axis++) {
            delta[axis] = to[axis] - from[axis];
            if (Math.abs(delta[axis]) > Math.abs(delta[major])) {
                major = axis;
            }
        }
        if (delta[major] == 0) {
            return -1;
        }
        int axisA = OTHER_AXES[major];
        int axisB = OTHER_AXES[major + 3];
        int direction = delta[major] > 0 ? 1 : -1;
        double slopeA = (double) delta[axisA] / (double) delta[major];
        double slopeB = (double) delta[axisB] / (double) delta[major];
        int[] point = {0, 0, 0};
        int i = 0;
        int end = delta[major] + direction;
        for (; i != end; i += direction) {
            point[major] = from[major] + i;
            point[axisA] = BetaMath.floor((double) from[axisA] + (double) i * slopeA);
            point[axisB] = BetaMath.floor((double) from[axisB] + (double) i * slopeB);
            int id = world.getBlockId(point[0], point[1], point[2]);
            if (id != 0 && id != BetaBlocks.LEAVES) {
                break;
            }
        }
        if (i == end) {
            return -1;
        }
        return Math.abs(i);
    }

    private boolean validTreeLocation() {
        int[] from = {base[0], base[1], base[2]};
        int[] to = {base[0], base[1] + heightLimit - 1, base[2]};
        int ground = world.getBlockId(base[0], base[1] - 1, base[2]);
        if (ground != BetaBlocks.GRASS && ground != BetaBlocks.DIRT) {
            return false;
        }
        int clear = checkBlockLine(from, to);
        if (clear == -1) {
            return true;
        }
        if (clear < 6) {
            return false;
        }
        heightLimit = clear;
        return true;
    }
}
