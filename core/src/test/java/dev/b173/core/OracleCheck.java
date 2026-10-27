package dev.b173.core;

import dev.b173.core.populate.BetaPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public final class OracleCheck {
    private OracleCheck() {
    }

    public static void main(String[] args) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(args[0])))) {
            byte[] magic = new byte[8];
            in.readFully(magic);
            if (!new String(magic, StandardCharsets.US_ASCII).equals("B173ORCL") || in.readInt() != 2) {
                throw new IOException("not a BetaOracle dump");
            }
            long seed = in.readLong();
            int minX = in.readInt();
            int minZ = in.readInt();
            int maxX = in.readInt();
            int maxZ = in.readInt();
            int size = (maxX - minX + 1) * 16 * (maxZ - minZ + 1) * 16 * 128;
            byte[] baseIds = read(in, size);
            byte[] baseMeta = read(in, size);
            byte[] ids = read(in, size);
            byte[] meta = read(in, size);
            byte[] skyLight = read(in, size);
            byte[] blockLight = read(in, size);
            int tileCount = in.readInt();
            TreeSet<String> betaTiles = new TreeSet<>();
            for (int i = 0; i < tileCount; i++) {
                betaTiles.add(in.readUTF());
            }

            BetaTerrain terrain = new BetaTerrain(seed);
            ChunkMapWorld world = new ChunkMapWorld(terrain, BetaWorld.Rules.FAITHFUL);
            for (int x = minX - 2; x <= maxX + 2; x++) {
                for (int z = minZ - 2; z <= maxZ + 2; z++) {
                    world.chunk(x, z);
                }
            }
            System.out.printf("seed %d, chunks %d..%d x %d..%d%n", seed, minX, maxX, minZ, maxZ);
            compare("base terrain", world, minX, minZ, maxX, maxZ, baseIds, baseMeta);

            List<int[]> order = new ArrayList<>();
            if (args.length > 1) {
                java.util.regex.Pattern line = java.util.regex.Pattern.compile("b173 populate (-?\\d+) (-?\\d+)");
                for (String text : java.nio.file.Files.readAllLines(new File(args[1]).toPath(), StandardCharsets.UTF_8)) {
                    java.util.regex.Matcher m = line.matcher(text);
                    if (m.find()) {
                        order.add(new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))});
                    }
                }
            } else {
                for (int x = minX - 2; x <= maxX + 1; x++) {
                    for (int z = minZ - 2; z <= maxZ + 1; z++) {
                        order.add(new int[]{x, z});
                    }
                }
            }
            BetaPopulator populator = new BetaPopulator(terrain);
            for (int[] chunk : order) {
                populator.populate(world, chunk[0], chunk[1]);
            }
            compare("decorated (" + order.size() + " chunks)", world, minX, minZ, maxX, maxZ, ids, meta);
            compareLight(world, minX, minZ, maxX, maxZ, ids, skyLight, blockLight);

            TreeSet<String> ourTiles = tiles(world, minX, minZ, maxX, maxZ);
            TreeSet<String> onlyBeta = new TreeSet<>(betaTiles);
            onlyBeta.removeAll(ourTiles);
            TreeSet<String> onlyOurs = new TreeSet<>(ourTiles);
            onlyOurs.removeAll(betaTiles);
            System.out.printf("tile entities: Beta %d, core %d, identical %d%n", betaTiles.size(), ourTiles.size(),
                    betaTiles.size() - onlyBeta.size());
            onlyBeta.forEach(t -> System.out.println("  only in Beta: " + t));
            onlyOurs.forEach(t -> System.out.println("  only in core: " + t));
        }
    }

    private static byte[] read(DataInputStream in, int size) throws IOException {
        byte[] bytes = new byte[size];
        in.readFully(bytes);
        return bytes;
    }

    private static void compare(String title, ChunkMapWorld world, int minX, int minZ, int maxX, int maxZ,
                                byte[] ids, byte[] meta) {
        Map<String, Integer> kinds = new TreeMap<>();
        List<String> idSamples = new ArrayList<>();
        List<String> metaSamples = new ArrayList<>();
        long idDiffs = 0;
        long metaDiffs = 0;
        int i = 0;
        for (int x = minX * 16; x < (maxX + 1) * 16; x++) {
            for (int z = minZ * 16; z < (maxZ + 1) * 16; z++) {
                for (int y = 0; y < 128; y++, i++) {
                    int betaId = ids[i] & 255;
                    int betaMeta = meta[i] & 15;
                    int ourId = world.getBlockId(x, y, z);
                    int ourMeta = world.getBlockMetadata(x, y, z);
                    List<String> samples;
                    if (betaId != ourId) {
                        idDiffs++;
                        kinds.merge(BetaBlocks.name(betaId) + " -> " + BetaBlocks.name(ourId), 1, Integer::sum);
                        samples = idSamples;
                    } else if (betaMeta != ourMeta) {
                        metaDiffs++;
                        kinds.merge(BetaBlocks.name(betaId) + ":" + betaMeta + " -> :" + ourMeta, 1, Integer::sum);
                        samples = metaSamples;
                    } else {
                        continue;
                    }
                    if (samples.size() < 15) {
                        samples.add(String.format("%d %d %d: Beta %s:%d, core %s:%d", x, y, z,
                                BetaBlocks.name(betaId), betaMeta, BetaBlocks.name(ourId), ourMeta));
                    }
                }
            }
        }
        System.out.printf("%s: %d blocks, %d IDs differ, %d metadata differ%n", title, ids.length, idDiffs, metaDiffs);
        kinds.entrySet().stream().sorted((p, q) -> q.getValue() - p.getValue()).limit(20)
                .forEach(e -> System.out.printf("  %8d  %s (Beta -> core)%n", e.getValue(), e.getKey()));
        idSamples.forEach(s -> System.out.println("  ID at " + s));
        metaSamples.forEach(s -> System.out.println("  metadata at " + s));
    }

    private static void compareLight(ChunkMapWorld world, int minX, int minZ, int maxX, int maxZ,
                                     byte[] ids, byte[] skyLight, byte[] blockLight) {
        long differ = 0;
        long surfaces = 0;
        long surfaceDiffer = 0;
        long blockLit = 0;
        List<String> samples = new ArrayList<>();
        int i = 0;
        for (int x = minX * 16; x < (maxX + 1) * 16; x++) {
            for (int z = minZ * 16; z < (maxZ + 1) * 16; z++) {
                for (int y = 0; y < 128; y++, i++) {
                    if (blockLight[i] != 0) {
                        blockLit++;
                    }
                    int beta = skyLight[i];
                    int ours = world.skyLight(x, y, z);
                    boolean surface = y > 0 && ids[i] == 0 && BetaBlocks.isSolid(ids[i - 1] & 255);
                    if (surface) {
                        surfaces++;
                    }
                    if (beta != ours) {
                        differ++;
                        if (surface) {
                            surfaceDiffer++;
                            if (samples.size() < 10) {
                                samples.add(String.format("%d %d %d: Beta %d, core %d", x, y, z, beta, ours));
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("sky light: %d of %d positions differ; on %d open surfaces, %d differ; block light non-zero at %d%n",
                differ, ids.length, surfaces, surfaceDiffer, blockLit);
        samples.forEach(s -> System.out.println("  light at " + s));
    }

    static TreeSet<String> tiles(ChunkMapWorld world, int minX, int minZ, int maxX, int maxZ) {
        TreeSet<String> lines = new TreeSet<>();
        for (Map.Entry<Long, List<BetaWorld.ChestItem>> chest : world.chests().entrySet()) {
            int x = BetaWorld.unpackX(chest.getKey());
            int y = BetaWorld.unpackY(chest.getKey());
            int z = BetaWorld.unpackZ(chest.getKey());
            if (!inside(x, z, minX, minZ, maxX, maxZ)) {
                continue;
            }
            String[] slots = new String[27];
            for (BetaWorld.ChestItem item : chest.getValue()) {
                slots[item.slot()] = item.slot() + ":" + betaItem(item.item()) + ":" + item.count() + ":" + item.damage();
            }
            StringBuilder line = new StringBuilder("chest " + x + " " + y + " " + z);
            for (String slot : slots) {
                if (slot != null) {
                    line.append(' ').append(slot);
                }
            }
            lines.add(line.toString());
        }
        for (Map.Entry<Long, String> spawner : world.spawners().entrySet()) {
            int x = BetaWorld.unpackX(spawner.getKey());
            int y = BetaWorld.unpackY(spawner.getKey());
            int z = BetaWorld.unpackZ(spawner.getKey());
            if (inside(x, z, minX, minZ, maxX, maxZ)) {
                lines.add("spawner " + x + " " + y + " " + z + " " + betaMob(spawner.getValue()));
            }
        }
        return lines;
    }

    private static boolean inside(int x, int z, int minX, int minZ, int maxX, int maxZ) {
        return x >> 4 >= minX && x >> 4 <= maxX && z >> 4 >= minZ && z >> 4 <= maxZ;
    }

    private static int betaItem(String item) {
        return switch (item) {
            case "minecraft:saddle" -> 329;
            case "minecraft:iron_ingot" -> 265;
            case "minecraft:bread" -> 297;
            case "minecraft:wheat" -> 296;
            case "minecraft:gunpowder" -> 289;
            case "minecraft:string" -> 287;
            case "minecraft:bucket" -> 325;
            case "minecraft:golden_apple" -> 322;
            case "minecraft:redstone" -> 331;
            case "minecraft:music_disc_13" -> 2256;
            case "minecraft:music_disc_cat" -> 2257;
            case "minecraft:cocoa_beans" -> 351;
            default -> -1;
        };
    }

    private static String betaMob(String mob) {
        return switch (mob) {
            case "minecraft:skeleton" -> "Skeleton";
            case "minecraft:spider" -> "Spider";
            case "minecraft:zombie" -> "Zombie";
            default -> mob;
        };
    }
}
