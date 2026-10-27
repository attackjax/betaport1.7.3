package dev.b173.core;

import dev.b173.core.populate.BetaHellPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public final class NetherParityTest {
    private static final long[][] VECTORS = {
            {12345L, -8, -8, 8, 8, 0x73138a869d926cb4L, 0x2fc13a7eb0cd2426L},
            {-4172144997902289642L, 20, -30, 30, -20, 0xb3eb038683e45087L, 0x9ac51067b217b9c7L},
            {3257840388504953787L, -40, -5, -30, 5, 0xd6bce10ddcb0c803L, 0xb7907523946313c4L},
    };

    private NetherParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("Nether against Beta 1.7.3's ChunkProviderHell (oracle hashes)");
        for (long[] v : VECTORS) {
            long seed = v[0];
            int minX = (int) v[1];
            int minZ = (int) v[2];
            int maxX = (int) v[3];
            int maxZ = (int) v[4];
            ChunkMapWorld world = new ChunkMapWorld(new BetaHell(seed), BetaWorld.Rules.FAITHFUL);
            for (int x = minX - 2; x <= maxX + 2; x++) {
                for (int z = minZ - 2; z <= maxZ + 2; z++) {
                    world.chunk(x, z);
                }
            }
            long base = hash(world, minX, minZ, maxX, maxZ);
            for (int x = minX - 2; x <= maxX + 1; x++) {
                for (int z = minZ - 2; z <= maxZ + 1; z++) {
                    BetaHellPopulator.populate(world, x, z);
                }
            }
            long decorated = hash(world, minX, minZ, maxX, maxZ);
            String area = String.format("seed %d, chunks %d..%d x %d..%d", seed, minX, maxX, minZ, maxZ);
            t.check(area + ": base terrain", base == v[5], String.format("hash %016x", base));
            t.check(area + ": decorated", decorated == v[6], String.format("hash %016x", decorated));
        }
    }

    static long hash(ChunkMapWorld world, int minX, int minZ, int maxX, int maxZ) {
        long ids = 0xCBF29CE484222325L;
        long meta = 0xCBF29CE484222325L;
        for (int x = minX * 16; x < (maxX + 1) * 16; x++) {
            for (int z = minZ * 16; z < (maxZ + 1) * 16; z++) {
                for (int y = 0; y < 128; y++) {
                    ids = (ids ^ (world.getBlockId(x, y, z) & 255)) * 0x100000001B3L;
                    meta = (meta ^ (world.getBlockMetadata(x, y, z) & 255)) * 0x100000001B3L;
                }
            }
        }
        return ids * 31L + meta;
    }

    private static long hash(byte[] ids, byte[] meta) {
        long a = 0xCBF29CE484222325L;
        long b = 0xCBF29CE484222325L;
        for (int i = 0; i < ids.length; i++) {
            a = (a ^ (ids[i] & 255)) * 0x100000001B3L;
            b = (b ^ (meta[i] & 255)) * 0x100000001B3L;
        }
        return a * 31L + b;
    }

    public static void main(String[] args) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(args[0])))) {
            in.readFully(new byte[8]);
            in.readInt();
            long seed = in.readLong();
            int minX = in.readInt();
            int minZ = in.readInt();
            int maxX = in.readInt();
            int maxZ = in.readInt();
            in.readBoolean();
            in.readBoolean();
            int size = (maxX - minX + 1) * 16 * (maxZ - minZ + 1) * 16 * 128;
            byte[][] parts = new byte[4][size];
            for (byte[] part : parts) {
                in.readFully(part);
            }
            System.out.printf("{%dL, %d, %d, %d, %d, 0x%016xL, 0x%016xL},%n", seed, minX, minZ, maxX, maxZ,
                    hash(parts[0], parts[1]), hash(parts[2], parts[3]));
        }
    }
}
