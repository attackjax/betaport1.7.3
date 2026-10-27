package dev.b173.core;

import dev.b173.core.populate.BetaPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class WorldCheck {
    private WorldCheck() {
    }

    public static void main(String[] args) throws IOException {
        long seed = Long.parseLong(args[0]);
        File regionDir = new File(args[1]);
        int minX = Integer.parseInt(args[2]);
        int minZ = Integer.parseInt(args[3]);
        int maxX = Integer.parseInt(args[4]);
        int maxZ = Integer.parseInt(args[5]);
        String outPrefix = args[6];
        int[] grassMap = null;
        int[] foliageMap = null;
        List<int[]> order = null;
        for (int i = 7; i < args.length; i++) {
            if (args[i].equals("--order")) {
                order = readOrder(new File(args[++i]));
            } else if (args[i].equals("--colours")) {
                grassMap = load(args[++i]);
                foliageMap = load(args[++i]);
            } else {
                throw new IllegalArgumentException("unknown option " + args[i]);
            }
        }

        Map<Long, SavedChunk> saved = new HashMap<>();
        Map<String, Integer> statuses = new TreeMap<>();
        for (int rx = Math.floorDiv(minX, 32); rx <= Math.floorDiv(maxX, 32); rx++) {
            for (int rz = Math.floorDiv(minZ, 32); rz <= Math.floorDiv(maxZ, 32); rz++) {
                File file = new File(regionDir, "r." + rx + "." + rz + ".mca");
                if (!file.isFile()) {
                    continue;
                }
                for (Map<String, Object> root : readRegion(file)) {
                    SavedChunk chunk = SavedChunk.of(root);
                    statuses.merge(chunk.status, 1, Integer::sum);
                    saved.put(key(chunk.x, chunk.z), chunk);
                }
            }
        }
        System.out.println("chunks in region files by status: " + statuses);

        BetaTerrain terrain = new BetaTerrain(seed);
        ChunkMapWorld reference = new ChunkMapWorld(terrain, BetaWorld.Rules.FAITHFUL);
        BetaPopulator populator = new BetaPopulator(terrain);
        for (int x = minX - 2; x <= maxX + 2; x++) {
            for (int z = minZ - 2; z <= maxZ + 2; z++) {
                reference.chunk(x, z);
            }
        }
        if (order == null) {
            for (int x = minX - 2; x <= maxX + 1; x++) {
                for (int z = minZ - 2; z <= maxZ + 1; z++) {
                    populator.populate(reference, x, z);
                }
            }
        } else {
            for (int[] chunk : order) {
                populator.populate(reference, chunk[0], chunk[1]);
            }
            System.out.println("reference decorated " + order.size() + " chunks in the logged order");
        }

        Map<String, Integer> kinds = new TreeMap<>();
        Map<String, Integer> metaKinds = new TreeMap<>();
        Map<String, Integer> unknown = new TreeMap<>();
        long compared = 0;
        long differing = 0;
        long metaDiffering = 0;
        long settling = 0;
        List<String> generationSamples = new ArrayList<>();
        int missing = 0;
        int x0 = minX * 16;
        int z0 = minZ * 16;
        int width = (maxX - minX + 1) * 16;
        int depth = (maxZ - minZ + 1) * 16;
        BufferedImage diffMap = new BufferedImage(width, depth, BufferedImage.TYPE_INT_RGB);
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                SavedChunk chunk = saved.get(key(cx, cz));
                if (chunk == null || !chunk.status.endsWith("full")) {
                    missing++;
                    continue;
                }
                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        int x = cx * 16 + lx;
                        int z = cz * 16 + lz;
                        int columnDiffs = 0;
                        for (int y = 0; y < 128; y++) {
                            String state = chunk.state(lx, y, lz);
                            int ours = toBeta(state);
                            if (ours < 0) {
                                unknown.merge(state, 1, Integer::sum);
                            }
                            int theirs = reference.getBlockId(x, y, z) | reference.getBlockMetadata(x, y, z) << 8;
                            compared++;
                            int a = foldId(theirs);
                            int b = ours < 0 ? -1 : foldId(ours);
                            if (a != b) {
                                differing++;
                                columnDiffs++;
                                if (settles(a) && settles(b)) {
                                    settling++;
                                } else if (generationSamples.size() < 40) {
                                    generationSamples.add(String.format("%d %d %d: %s -> %s", x, y, z,
                                            BetaBlocks.name(a & 255), b < 0 ? state : BetaBlocks.name(b & 255)));
                                }
                                kinds.merge(BetaBlocks.name(a & 255) + " -> " + (b < 0 ? state : BetaBlocks.name(b & 255)), 1, Integer::sum);
                            } else if (ours >= 0 && foldMeta(theirs) != foldMeta(ours)) {
                                metaDiffering++;
                                metaKinds.merge(BetaBlocks.name(a & 255) + ":" + foldMeta(theirs) + " -> " + foldMeta(ours), 1, Integer::sum);
                            }
                        }
                        diffMap.setRGB(x - x0, z - z0, columnDiffs == 0 ? 0x202020 : columnDiffs < 3 ? 0xE0A000 : 0xFF2020);
                    }
                }
            }
        }
        System.out.printf("compared %d blocks in chunks %d..%d x %d..%d (%d chunks missing or not full)%n",
                compared, minX, maxX, minZ, maxZ, missing);
        System.out.printf("block IDs differing: %d (%.4f%%)%n", differing, 100.0 * differing / Math.max(1, compared));
        System.out.printf("  of which settling after generation (sand and gravel falling, liquids flowing on): %d%n", settling);
        System.out.printf("  of which generation itself: %d (%.4f%%)%n", differing - settling,
                100.0 * (differing - settling) / Math.max(1, compared));
        print("by kind (reference -> saved)", kinds);
        if (!generationSamples.isEmpty()) {
            System.out.println("first generation differences (x y z):");
            generationSamples.forEach(line -> System.out.println("  " + line));
        }
        System.out.printf("same ID, different metadata: %d%n", metaDiffering);
        print("metadata (reference -> saved)", metaKinds);
        if (!unknown.isEmpty()) {
            print("states with no Beta equivalent", unknown);
        }

        ImageIO.write(diffMap, "png", new File(outPrefix + "-diff.png"));
        BufferedImage savedImage = MapRender.render(new MapRender.Blocks() {
            @Override
            public int id(int x, int y, int z) {
                SavedChunk chunk = saved.get(key(x >> 4, z >> 4));
                int beta = chunk == null ? 0 : toBeta(chunk.state(x & 15, y, z & 15));
                return beta < 0 ? BetaBlocks.STONE : beta & 255;
            }

            @Override
            public int meta(int x, int y, int z) {
                SavedChunk chunk = saved.get(key(x >> 4, z >> 4));
                int beta = chunk == null ? 0 : toBeta(chunk.state(x & 15, y, z & 15));
                return beta < 0 ? 0 : beta >>> 8 & 15;
            }
        }, terrain.climate(), x0, z0, Math.min(width, depth), grassMap, foliageMap);
        ImageIO.write(savedImage, "png", new File(outPrefix + "-saved.png"));
        BufferedImage referenceImage = MapRender.render(new MapRender.Blocks() {
            @Override
            public int id(int x, int y, int z) {
                return reference.getBlockId(x, y, z);
            }

            @Override
            public int meta(int x, int y, int z) {
                return reference.getBlockMetadata(x, y, z);
            }
        }, terrain.climate(), x0, z0, Math.min(width, depth), grassMap, foliageMap);
        ImageIO.write(referenceImage, "png", new File(outPrefix + "-reference.png"));
        System.out.println("wrote " + outPrefix + "-{saved,reference,diff}.png");
    }

    private static void print(String title, Map<String, Integer> counts) {
        System.out.println(title + ":");
        counts.entrySet().stream()
                .sorted((p, q) -> q.getValue() - p.getValue())
                .limit(25)
                .forEach(e -> System.out.printf("  %8d  %s%n", e.getValue(), e.getKey()));
    }

    private static long key(int x, int z) {
        return (long) x << 32 ^ (z & 0xFFFFFFFFL);
    }

    private static boolean settles(int id) {
        return switch (id) {
            case BetaBlocks.AIR, BetaBlocks.WATER, BetaBlocks.LAVA, BetaBlocks.SAND, BetaBlocks.GRAVEL,
                 BetaBlocks.OBSIDIAN, BetaBlocks.COBBLESTONE -> true;
            default -> false;
        };
    }

    private static int foldId(int packed) {
        int id = packed & 255;
        int meta = packed >>> 8 & 15;
        if (id == BetaBlocks.FLOWING_WATER) {
            return BetaBlocks.WATER;
        }
        if (id == BetaBlocks.FLOWING_LAVA) {
            return BetaBlocks.LAVA;
        }
        if (id == BetaBlocks.TALL_GRASS && meta == BetaBlocks.TALL_GRASS_SHRUB) {
            return BetaBlocks.DEAD_BUSH;
        }
        return id;
    }

    private static int foldMeta(int packed) {
        int id = packed & 255;
        int meta = packed >>> 8 & 15;
        return switch (id) {
            case BetaBlocks.LOG, BetaBlocks.LEAVES -> meta & 3;
            case BetaBlocks.TALL_GRASS, BetaBlocks.WATER, BetaBlocks.FLOWING_WATER,
                 BetaBlocks.LAVA, BetaBlocks.FLOWING_LAVA, BetaBlocks.PUMPKIN -> meta;
            default -> 0;
        };
    }

    static int toBeta(String state) {
        int bracket = state.indexOf('[');
        String name = bracket < 0 ? state : state.substring(0, bracket);
        String props = bracket < 0 ? "" : state.substring(bracket);
        return switch (name) {
            case "minecraft:air", "minecraft:cave_air", "minecraft:void_air" -> BetaBlocks.AIR;
            case "minecraft:stone" -> BetaBlocks.STONE;
            case "minecraft:grass_block" -> BetaBlocks.GRASS;
            case "minecraft:dirt" -> BetaBlocks.DIRT;
            case "minecraft:cobblestone" -> BetaBlocks.COBBLESTONE;
            case "minecraft:bedrock" -> BetaBlocks.BEDROCK;
            case "minecraft:water" -> liquid(BetaBlocks.WATER, BetaBlocks.FLOWING_WATER, props);
            case "minecraft:lava" -> liquid(BetaBlocks.LAVA, BetaBlocks.FLOWING_LAVA, props);
            case "minecraft:sand" -> BetaBlocks.SAND;
            case "minecraft:gravel" -> BetaBlocks.GRAVEL;
            case "minecraft:gold_ore" -> BetaBlocks.GOLD_ORE;
            case "minecraft:iron_ore" -> BetaBlocks.IRON_ORE;
            case "minecraft:coal_ore" -> BetaBlocks.COAL_ORE;
            case "minecraft:oak_log" -> BetaBlocks.LOG | BetaBlocks.WOOD_OAK << 8;
            case "minecraft:spruce_log" -> BetaBlocks.LOG | BetaBlocks.WOOD_SPRUCE << 8;
            case "minecraft:birch_log" -> BetaBlocks.LOG | BetaBlocks.WOOD_BIRCH << 8;
            case "minecraft:oak_leaves" -> BetaBlocks.LEAVES | BetaBlocks.WOOD_OAK << 8;
            case "minecraft:spruce_leaves" -> BetaBlocks.LEAVES | BetaBlocks.WOOD_SPRUCE << 8;
            case "minecraft:birch_leaves" -> BetaBlocks.LEAVES | BetaBlocks.WOOD_BIRCH << 8;
            case "minecraft:lapis_ore" -> BetaBlocks.LAPIS_ORE;
            case "minecraft:sandstone" -> BetaBlocks.SANDSTONE;
            case "minecraft:short_grass" -> BetaBlocks.TALL_GRASS | BetaBlocks.TALL_GRASS_GRASS << 8;
            case "minecraft:fern" -> BetaBlocks.TALL_GRASS | BetaBlocks.TALL_GRASS_FERN << 8;
            case "minecraft:dead_bush" -> BetaBlocks.DEAD_BUSH;
            case "minecraft:dandelion" -> BetaBlocks.DANDELION;
            case "minecraft:poppy" -> BetaBlocks.ROSE;
            case "minecraft:brown_mushroom" -> BetaBlocks.BROWN_MUSHROOM;
            case "minecraft:red_mushroom" -> BetaBlocks.RED_MUSHROOM;
            case "minecraft:mossy_cobblestone" -> BetaBlocks.MOSSY_COBBLESTONE;
            case "minecraft:obsidian" -> BetaBlocks.OBSIDIAN;
            case "minecraft:spawner" -> BetaBlocks.SPAWNER;
            case "minecraft:chest" -> BetaBlocks.CHEST;
            case "minecraft:diamond_ore" -> BetaBlocks.DIAMOND_ORE;
            case "minecraft:redstone_ore" -> BetaBlocks.REDSTONE_ORE;
            case "minecraft:snow" -> BetaBlocks.SNOW_LAYER;
            case "minecraft:ice" -> BetaBlocks.ICE;
            case "minecraft:snow_block" -> BetaBlocks.SNOW_BLOCK;
            case "minecraft:cactus" -> BetaBlocks.CACTUS;
            case "minecraft:clay" -> BetaBlocks.CLAY;
            case "minecraft:sugar_cane" -> BetaBlocks.REEDS;
            case "minecraft:carved_pumpkin" -> BetaBlocks.PUMPKIN | pumpkinMeta(props) << 8;
            default -> -1;
        };
    }

    private static int liquid(int still, int flowing, String props) {
        int level = intProperty(props, "level");
        return level == 0 ? still : flowing | (level & 15) << 8;
    }

    private static int pumpkinMeta(String props) {
        String facing = property(props, "facing");
        return switch (facing == null ? "" : facing) {
            case "south" -> 0;
            case "west" -> 1;
            case "north" -> 2;
            default -> 3;
        };
    }

    private static int intProperty(String props, String name) {
        String value = property(props, name);
        return value == null ? 0 : Integer.parseInt(value);
    }

    private static String property(String props, String name) {
        int at = props.indexOf(name + "=");
        if (at < 0) {
            return null;
        }
        int end = at + name.length() + 1;
        while (end < props.length() && props.charAt(end) != ',' && props.charAt(end) != ']') {
            end++;
        }
        return props.substring(at + name.length() + 1, end);
    }

    private static List<int[]> readOrder(File log) throws IOException {
        List<int[]> order = new ArrayList<>();
        java.util.regex.Pattern line = java.util.regex.Pattern.compile("b173 populate (-?\\d+) (-?\\d+)");
        for (String text : java.nio.file.Files.readAllLines(log.toPath(), StandardCharsets.UTF_8)) {
            java.util.regex.Matcher m = line.matcher(text);
            if (m.find()) {
                order.add(new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))});
            }
        }
        return order;
    }

    private static int[] load(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        return image.getRGB(0, 0, 256, 256, null, 0, 256);
    }

    static final class SavedChunk {
        final int x;
        final int z;
        final String status;
        final Map<Integer, String[]> sectionPalettes = new HashMap<>();
        final Map<Integer, long[]> sectionData = new HashMap<>();

        private SavedChunk(int x, int z, String status) {
            this.x = x;
            this.z = z;
            this.status = status;
        }

        @SuppressWarnings("unchecked")
        static SavedChunk of(Map<String, Object> root) {
            SavedChunk chunk = new SavedChunk((Integer) root.get("xPos"), (Integer) root.get("zPos"), (String) root.get("Status"));
            List<Object> sections = (List<Object>) root.getOrDefault("sections", List.of());
            for (Object o : sections) {
                Map<String, Object> section = (Map<String, Object>) o;
                int y = (Byte) section.get("Y");
                Map<String, Object> states = (Map<String, Object>) section.get("block_states");
                if (states == null) {
                    continue;
                }
                List<Object> palette = (List<Object>) states.get("palette");
                String[] names = new String[palette.size()];
                for (int i = 0; i < names.length; i++) {
                    Map<String, Object> entry = (Map<String, Object>) palette.get(i);
                    StringBuilder name = new StringBuilder((String) entry.get("Name"));
                    Map<String, Object> props = (Map<String, Object>) entry.get("Properties");
                    if (props != null && !props.isEmpty()) {
                        name.append('[');
                        new TreeMap<>(props).forEach((k, v) -> name.append(k).append('=').append(v).append(','));
                        name.setCharAt(name.length() - 1, ']');
                    }
                    names[i] = name.toString();
                }
                chunk.sectionPalettes.put(y, names);
                chunk.sectionData.put(y, (long[]) states.get("data"));
            }
            return chunk;
        }

        String state(int x, int y, int z) {
            String[] palette = sectionPalettes.get(y >> 4);
            if (palette == null) {
                return "minecraft:air";
            }
            long[] data = sectionData.get(y >> 4);
            if (data == null) {
                return palette[0];
            }
            int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(palette.length - 1));
            int perLong = 64 / bits;
            int index = (y & 15) << 8 | z << 4 | x;
            long word = data[index / perLong];
            int value = (int) (word >>> (index % perLong) * bits & (1L << bits) - 1);
            return palette[value];
        }
    }

    static List<Map<String, Object>> readRegion(File file) throws IOException {
        List<Map<String, Object>> chunks = new ArrayList<>();
        try (RandomAccessFile region = new RandomAccessFile(file, "r")) {
            int[] locations = new int[1024];
            for (int i = 0; i < 1024; i++) {
                locations[i] = region.readInt();
            }
            for (int location : locations) {
                if (location == 0) {
                    continue;
                }
                region.seek((long) (location >>> 8) * 4096L);
                int length = region.readInt();
                int compression = region.readUnsignedByte();
                byte[] payload = new byte[length - 1];
                region.readFully(payload);
                InputStream in = switch (compression) {
                    case 1 -> new GZIPInputStream(new ByteArrayInputStream(payload));
                    case 2 -> new InflaterInputStream(new ByteArrayInputStream(payload));
                    case 3 -> new ByteArrayInputStream(payload);
                    default -> throw new IOException("unsupported chunk compression " + compression + " in " + file);
                };
                try (DataInputStream data = new DataInputStream(in)) {
                    int type = data.readUnsignedByte();
                    if (type != 10) {
                        throw new IOException("chunk root is not a compound");
                    }
                    data.skipBytes(data.readUnsignedShort());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> root = (Map<String, Object>) readTag(data, 10);
                    chunks.add(root);
                }
            }
        }
        return chunks;
    }

    private static Object readTag(DataInputStream in, int type) throws IOException {
        return switch (type) {
            case 1 -> in.readByte();
            case 2 -> in.readShort();
            case 3 -> in.readInt();
            case 4 -> in.readLong();
            case 5 -> in.readFloat();
            case 6 -> in.readDouble();
            case 7 -> {
                byte[] bytes = new byte[in.readInt()];
                in.readFully(bytes);
                yield bytes;
            }
            case 8 -> {
                byte[] bytes = new byte[in.readUnsignedShort()];
                in.readFully(bytes);
                yield new String(bytes, StandardCharsets.UTF_8);
            }
            case 9 -> {
                int elementType = in.readUnsignedByte();
                int count = in.readInt();
                List<Object> list = new ArrayList<>(Math.max(0, count));
                for (int i = 0; i < count; i++) {
                    list.add(readTag(in, elementType));
                }
                yield list;
            }
            case 10 -> {
                Map<String, Object> compound = new LinkedHashMap<>();
                while (true) {
                    int childType = in.readUnsignedByte();
                    if (childType == 0) {
                        break;
                    }
                    byte[] name = new byte[in.readUnsignedShort()];
                    in.readFully(name);
                    compound.put(new String(name, StandardCharsets.UTF_8), readTag(in, childType));
                }
                yield compound;
            }
            case 11 -> {
                int[] ints = new int[in.readInt()];
                for (int i = 0; i < ints.length; i++) {
                    ints[i] = in.readInt();
                }
                yield ints;
            }
            case 12 -> {
                long[] longs = new long[in.readInt()];
                for (int i = 0; i < longs.length; i++) {
                    longs[i] = in.readLong();
                }
                yield longs;
            }
            default -> throw new IOException("unknown NBT tag type " + type);
        };
    }
}
