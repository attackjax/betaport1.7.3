package dev.b173.core;

import dev.b173.core.populate.BetaHellPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public final class NetherOracleCheck {
    private NetherOracleCheck() {
    }

    public static void main(String[] args) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(args[0])))) {
            byte[] magic = new byte[8];
            in.readFully(magic);
            if (!new String(magic, StandardCharsets.US_ASCII).equals("B173NETH") || in.readInt() != 1) {
                throw new IOException("not a NetherOracle dump");
            }
            long seed = in.readLong();
            int minX = in.readInt();
            int minZ = in.readInt();
            int maxX = in.readInt();
            int maxZ = in.readInt();
            boolean arrival = in.readBoolean();
            boolean light = in.readBoolean();
            int size = (maxX - minX + 1) * 16 * (maxZ - minZ + 1) * 16 * 128;
            byte[] baseIds = read(in, size);
            byte[] baseMeta = read(in, size);
            byte[] ids = read(in, size);
            byte[] meta = read(in, size);

            BetaHell hell = new BetaHell(seed);
            ChunkMapWorld world = new ChunkMapWorld(hell, BetaWorld.Rules.FAITHFUL);
            for (int x = minX - 2; x <= maxX + 2; x++) {
                for (int z = minZ - 2; z <= maxZ + 2; z++) {
                    world.chunk(x, z);
                }
            }
            System.out.printf("seed %d, chunks %d..%d x %d..%d%s%s%n", seed, minX, maxX, minZ, maxZ,
                    arrival ? ", Beta in arrival order" : "", light ? " with its light queue" : "");
            int baseDiff = compare("base terrain", world, minX, minZ, maxX, maxZ, baseIds, baseMeta);

            long start = System.nanoTime();
            int count = 0;
            for (int x = minX - 2; x <= maxX + 1; x++) {
                for (int z = minZ - 2; z <= maxZ + 1; z++) {
                    BetaHellPopulator.populate(world, x, z);
                    count++;
                }
            }
            System.out.printf("decorated %d chunks in %.2f s%n", count, (System.nanoTime() - start) / 1e9);
            int decoratedDiff = compare("decorated", world, minX, minZ, maxX, maxZ, ids, meta);
            if (!arrival && (baseDiff != 0 || decoratedDiff != 0)) {
                System.exit(1);
            }
        }
    }

    private static byte[] read(DataInputStream in, int size) throws IOException {
        byte[] bytes = new byte[size];
        in.readFully(bytes);
        return bytes;
    }

    private static int compare(String title, ChunkMapWorld world, int minX, int minZ, int maxX, int maxZ,
                               byte[] ids, byte[] meta) {
        int i = 0;
        int idDiff = 0;
        int metaDiff = 0;
        Map<String, Integer> kinds = new TreeMap<>();
        Map<Integer, Integer> betaCounts = new TreeMap<>();
        Map<Integer, Integer> coreCounts = new TreeMap<>();
        String first = null;
        for (int x = minX * 16; x < (maxX + 1) * 16; x++) {
            for (int z = minZ * 16; z < (maxZ + 1) * 16; z++) {
                for (int y = 0; y < 128; y++) {
                    int betaId = ids[i] & 255;
                    int coreId = world.getBlockId(x, y, z);
                    betaCounts.merge(betaId, 1, Integer::sum);
                    coreCounts.merge(coreId, 1, Integer::sum);
                    if (betaId != coreId) {
                        idDiff++;
                        kinds.merge(BetaBlocks.name(betaId) + " -> " + BetaBlocks.name(coreId), 1, Integer::sum);
                        if (first == null) {
                            first = x + " " + y + " " + z + ": Beta " + betaId + ", core " + coreId;
                        }
                    } else if ((meta[i] & 15) != (world.getBlockMetadata(x, y, z) & 15)) {
                        metaDiff++;
                        if (first == null) {
                            first = x + " " + y + " " + z + ": " + BetaBlocks.name(betaId) + " meta Beta " + meta[i]
                                    + ", core " + world.getBlockMetadata(x, y, z);
                        }
                    }
                    i++;
                }
            }
        }
        System.out.printf("%s: %d blocks, %d IDs differ, %d metadata differ%n", title, i, idDiff, metaDiff);
        if (first != null) {
            System.out.println("  first: " + first);
        }
        kinds.forEach((k, v) -> System.out.println("  " + k + ": " + v));
        System.out.println("  Beta counts: " + named(betaCounts));
        if (idDiff != 0) {
            System.out.println("  core counts: " + named(coreCounts));
        }
        return idDiff + metaDiff;
    }

    private static String named(Map<Integer, Integer> counts) {
        StringBuilder text = new StringBuilder();
        counts.forEach((id, n) -> text.append(BetaBlocks.name(id)).append('=').append(n).append(' '));
        return text.toString().trim();
    }
}
