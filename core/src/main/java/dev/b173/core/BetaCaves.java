package dev.b173.core;

final class BetaCaves {
    private static final int RANGE = 8;
    private static final float PI = (float) Math.PI;

    private final long seed;
    private final long xMultiplier;
    private final long zMultiplier;

    BetaCaves(long seed) {
        this.seed = seed;
        BetaRandom random = new BetaRandom(seed);
        xMultiplier = random.nextLong() / 2L * 2L + 1L;
        zMultiplier = random.nextLong() / 2L * 2L + 1L;
    }

    void carve(int chunkX, int chunkZ, byte[] blocks) {
        BetaRandom source = new BetaRandom(0L);
        for (int sx = chunkX - RANGE; sx <= chunkX + RANGE; sx++) {
            for (int sz = chunkZ - RANGE; sz <= chunkZ + RANGE; sz++) {
                source.setSeed((long) sx * xMultiplier + (long) sz * zMultiplier ^ seed);
                startSystems(source, sx, sz, chunkX, chunkZ, blocks);
            }
        }
    }

    private void startSystems(BetaRandom source, int sourceX, int sourceZ, int chunkX, int chunkZ, byte[] blocks) {
        int count = source.nextInt(source.nextInt(source.nextInt(40) + 1) + 1);
        if (source.nextInt(15) != 0) {
            count = 0;
        }
        for (int i = 0; i < count; i++) {
            double x = sourceX * 16 + source.nextInt(16);
            double y = source.nextInt(source.nextInt(120) + 8);
            double z = sourceZ * 16 + source.nextInt(16);
            int tunnels = 1;
            if (source.nextInt(4) == 0) {
                carveTunnel(source, chunkX, chunkZ, blocks, x, y, z, 1.0F + source.nextFloat() * 6.0F,
                        0.0F, 0.0F, -1, -1, 0.5D);
                tunnels += source.nextInt(4);
            }
            for (int t = 0; t < tunnels; t++) {
                float yaw = source.nextFloat() * PI * 2.0F;
                float pitch = (source.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float width = source.nextFloat() * 2.0F + source.nextFloat();
                carveTunnel(source, chunkX, chunkZ, blocks, x, y, z, width, yaw, pitch, 0, 0, 1.0D);
            }
        }
    }

    private void carveTunnel(BetaRandom source, int chunkX, int chunkZ, byte[] blocks,
                             double x, double y, double z, float width, float yaw, float pitch,
                             int step, int length, double verticalScale) {
        double centreX = chunkX * 16 + 8;
        double centreZ = chunkZ * 16 + 8;
        float yawVelocity = 0.0F;
        float pitchVelocity = 0.0F;
        BetaRandom random = new BetaRandom(source.nextLong());
        if (length <= 0) {
            int maximum = RANGE * 16 - 16;
            length = maximum - random.nextInt(maximum / 4);
        }
        boolean room = false;
        if (step == -1) {
            step = length / 2;
            room = true;
        }
        int branchAt = random.nextInt(length / 2) + length / 4;
        boolean steep = random.nextInt(6) == 0;
        for (; step < length; step++) {
            double radius = 1.5D + (double) (BetaMath.sin((float) step * PI / (float) length) * width * 1.0F);
            double radiusY = radius * verticalScale;
            float pitchCos = BetaMath.cos(pitch);
            float pitchSin = BetaMath.sin(pitch);
            x += BetaMath.cos(yaw) * pitchCos;
            y += pitchSin;
            z += BetaMath.sin(yaw) * pitchCos;
            if (steep) {
                pitch *= 0.92F;
            } else {
                pitch *= 0.7F;
            }
            pitch += pitchVelocity * 0.1F;
            yaw += yawVelocity * 0.1F;
            pitchVelocity *= 0.9F;
            yawVelocity *= 0.75F;
            pitchVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (!room && step == branchAt && width > 1.0F) {
                carveTunnel(source, chunkX, chunkZ, blocks, x, y, z, random.nextFloat() * 0.5F + 0.5F,
                        yaw - PI / 2.0F, pitch / 3.0F, step, length, 1.0D);
                carveTunnel(source, chunkX, chunkZ, blocks, x, y, z, random.nextFloat() * 0.5F + 0.5F,
                        yaw + PI / 2.0F, pitch / 3.0F, step, length, 1.0D);
                return;
            }
            if (!room && random.nextInt(4) == 0) {
                continue;
            }
            double dx = x - centreX;
            double dz = z - centreZ;
            double remaining = length - step;
            double reach = width + 2.0F + 16.0F;
            if (dx * dx + dz * dz - remaining * remaining > reach * reach) {
                return;
            }
            if (x < centreX - 16.0D - radius * 2.0D || z < centreZ - 16.0D - radius * 2.0D
                    || x > centreX + 16.0D + radius * 2.0D || z > centreZ + 16.0D + radius * 2.0D) {
                continue;
            }
            int minX = BetaMath.floor(x - radius) - chunkX * 16 - 1;
            int maxX = BetaMath.floor(x + radius) - chunkX * 16 + 1;
            int minY = BetaMath.floor(y - radiusY) - 1;
            int maxY = BetaMath.floor(y + radiusY) + 1;
            int minZ = BetaMath.floor(z - radius) - chunkZ * 16 - 1;
            int maxZ = BetaMath.floor(z + radius) - chunkZ * 16 + 1;
            if (minX < 0) {
                minX = 0;
            }
            if (maxX > 16) {
                maxX = 16;
            }
            if (minY < 1) {
                minY = 1;
            }
            if (maxY > 120) {
                maxY = 120;
            }
            if (minZ < 0) {
                minZ = 0;
            }
            if (maxZ > 16) {
                maxZ = 16;
            }
            if (touchesWater(blocks, minX, maxX, minY, maxY, minZ, maxZ)) {
                continue;
            }
            for (int bx = minX; bx < maxX; bx++) {
                double nx = ((double) (bx + chunkX * 16) + 0.5D - x) / radius;
                for (int bz = minZ; bz < maxZ; bz++) {
                    double nz = ((double) (bz + chunkZ * 16) + 0.5D - z) / radius;
                    if (nx * nx + nz * nz >= 1.0D) {
                        continue;
                    }
                    int index = (bx * 16 + bz) * 128 + maxY;
                    boolean grassAbove = false;
                    for (int by = maxY - 1; by >= minY; by--) {
                        double ny = ((double) by + 0.5D - y) / radiusY;
                        if (ny > -0.7D && nx * nx + ny * ny + nz * nz < 1.0D) {
                            int id = blocks[index];
                            if (id == BetaBlocks.GRASS) {
                                grassAbove = true;
                            }
                            if (id == BetaBlocks.STONE || id == BetaBlocks.DIRT || id == BetaBlocks.GRASS) {
                                if (by < 10) {
                                    blocks[index] = (byte) BetaBlocks.FLOWING_LAVA;
                                } else {
                                    blocks[index] = (byte) BetaBlocks.AIR;
                                    if (grassAbove && blocks[index - 1] == BetaBlocks.DIRT) {
                                        blocks[index - 1] = (byte) BetaBlocks.GRASS;
                                    }
                                }
                            }
                        }
                        index--;
                    }
                }
            }
            if (room) {
                break;
            }
        }
    }

    private static boolean touchesWater(byte[] blocks, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int bx = minX; bx < maxX; bx++) {
            for (int bz = minZ; bz < maxZ; bz++) {
                for (int by = maxY + 1; by >= minY - 1; by--) {
                    if (by >= 0 && by < 128) {
                        int id = blocks[(bx * 16 + bz) * 128 + by];
                        if (id == BetaBlocks.FLOWING_WATER || id == BetaBlocks.WATER) {
                            return true;
                        }
                        if (by != minY - 1 && bx != minX && bx != maxX - 1 && bz != minZ && bz != maxZ - 1) {
                            by = minY;
                        }
                    }
                }
            }
        }
        return false;
    }
}
