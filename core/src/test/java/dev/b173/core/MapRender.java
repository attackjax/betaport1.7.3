package dev.b173.core;

import dev.b173.core.populate.BetaPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class MapRender {
    private MapRender() {
    }

    public interface Blocks {
        int id(int x, int y, int z);

        int meta(int x, int y, int z);
    }

    public static void main(String[] args) throws IOException {
        long seed = Long.parseLong(args[0]);
        int centreX = Integer.parseInt(args[1]);
        int centreZ = Integer.parseInt(args[2]);
        int radius = Integer.parseInt(args[3]);
        File out = new File(args[4]);
        int[] grassMap = args.length > 6 ? load(args[5]) : null;
        int[] foliageMap = args.length > 6 ? load(args[6]) : null;

        BetaTerrain terrain = new BetaTerrain(seed);
        ChunkMapWorld world = new ChunkMapWorld(terrain, BetaWorld.Rules.FAITHFUL);
        BetaPopulator populator = new BetaPopulator(terrain);
        long start = System.nanoTime();
        for (int x = centreX - radius - 1; x <= centreX + radius + 1; x++) {
            for (int z = centreZ - radius - 1; z <= centreZ + radius + 1; z++) {
                world.chunk(x, z);
            }
        }
        for (int x = centreX - radius - 1; x <= centreX + radius; x++) {
            for (int z = centreZ - radius - 1; z <= centreZ + radius; z++) {
                populator.populate(world, x, z);
            }
        }
        System.out.printf("generated and populated %d chunks in %.1f s%n",
                (2 * radius + 3) * (2 * radius + 3), (System.nanoTime() - start) / 1e9);

        int x0 = (centreX - radius) * 16;
        int z0 = (centreZ - radius) * 16;
        int size = (2 * radius + 1) * 16;
        BufferedImage image = render(new Blocks() {
            @Override
            public int id(int x, int y, int z) {
                return world.getBlockId(x, y, z);
            }

            @Override
            public int meta(int x, int y, int z) {
                return world.getBlockMetadata(x, y, z);
            }
        }, terrain.climate(), x0, z0, size, grassMap, foliageMap);
        ImageIO.write(image, "png", out);
        System.out.println("wrote " + out + " (" + size + "x" + size + " blocks from " + x0 + "," + z0 + ")");
    }

    public static BufferedImage render(Blocks blocks, BetaClimate climate, int x0, int z0, int size,
                                       int[] grassMap, int[] foliageMap) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        double[] temperature = new double[size];
        double[] humidity = new double[size];
        int[] previousHeight = new int[size];
        for (int dz = -1; dz < size; dz++) {
            int z = z0 + dz;
            for (int dx = 0; dx < size; dx++) {
                int x = x0 + dx;
                int y = 127;
                int id = 0;
                while (y > 0) {
                    id = blocks.id(x, y, z);
                    if (id != 0 && id != BetaBlocks.SNOW_LAYER) {
                        break;
                    }
                    y--;
                }
                boolean snowy = y < 127 && blocks.id(x, y + 1, z) == BetaBlocks.SNOW_LAYER;
                if (dz < 0) {
                    previousHeight[dx] = y;
                    continue;
                }
                if (dx == 0) {
                    climate.sample(x, z, size, 1, temperature, humidity);
                }
                int colour = colour(id, blocks.meta(x, y, z), snowy, temperature[dx], humidity[dx], grassMap, foliageMap);
                double shade = y > previousHeight[dx] ? 1.0 : y < previousHeight[dx] ? 0.78 : 0.88;
                if (BetaBlocks.isWater(id)) {
                    int depth = 0;
                    while (depth < 24 && BetaBlocks.isWater(blocks.id(x, y - depth - 1, z))) {
                        depth++;
                    }
                    shade = 1.0 - depth * 0.025;
                }
                image.setRGB(dx, dz, scale(colour, shade));
                previousHeight[dx] = y;
            }
        }
        return image;
    }

    private static int colour(int id, int meta, boolean snowy, double t, double h,
                              int[] grassMap, int[] foliageMap) {
        if (snowy) {
            return 0xF4F8FF;
        }
        return switch (id) {
            case BetaBlocks.GRASS -> colourMap(grassMap, t, h, 0x7FB238);
            case BetaBlocks.LEAVES -> switch (meta & 3) {
                case BetaBlocks.WOOD_SPRUCE -> 0x619961;
                case BetaBlocks.WOOD_BIRCH -> 0x80A755;
                default -> scale(colourMap(foliageMap, t, h, 0x48B518), 0.82);
            };
            case BetaBlocks.WATER, BetaBlocks.FLOWING_WATER -> 0x3D5AFE;
            case BetaBlocks.LAVA, BetaBlocks.FLOWING_LAVA -> 0xFF5A00;
            case BetaBlocks.SAND -> 0xDBD3A0;
            case BetaBlocks.SANDSTONE -> 0xD2C79B;
            case BetaBlocks.GRAVEL -> 0x8C8682;
            case BetaBlocks.DIRT -> 0x866043;
            case BetaBlocks.STONE, BetaBlocks.COBBLESTONE, BetaBlocks.MOSSY_COBBLESTONE -> 0x7D7D7D;
            case BetaBlocks.ICE -> 0xA0C8FF;
            case BetaBlocks.SNOW_BLOCK -> 0xF4F8FF;
            case BetaBlocks.CLAY -> 0xA0A6B4;
            case BetaBlocks.LOG -> 0x664E33;
            case BetaBlocks.CACTUS -> 0x0D6A1A;
            case BetaBlocks.REEDS -> 0x95C36B;
            case BetaBlocks.PUMPKIN -> 0xE38A1D;
            case BetaBlocks.BEDROCK -> 0x333333;
            case BetaBlocks.OBSIDIAN -> 0x14121E;
            default -> 0x7D7D7D;
        };
    }

    private static int colourMap(int[] map, double t, double h, int fallback) {
        if (map == null) {
            return fallback;
        }
        h *= t;
        int i = (int) ((1.0D - t) * 255.0D);
        int j = (int) ((1.0D - h) * 255.0D);
        return map[j << 8 | i] & 0xFFFFFF;
    }

    private static int scale(int rgb, double factor) {
        int r = (int) Math.min(255, ((rgb >> 16) & 255) * factor);
        int g = (int) Math.min(255, ((rgb >> 8) & 255) * factor);
        int b = (int) Math.min(255, (rgb & 255) * factor);
        return r << 16 | g << 8 | b;
    }

    private static int[] load(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        return image.getRGB(0, 0, 256, 256, null, 0, 256);
    }
}
