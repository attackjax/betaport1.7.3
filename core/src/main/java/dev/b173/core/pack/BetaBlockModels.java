package dev.b173.core.pack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BetaBlockModels {
    static final String[] FACES = {"down", "up", "north", "south", "west", "east"};

    private static final int[][][] CORNERS = {
            {{0, 0, 1}, {0, 0, 0}, {1, 0, 0}, {1, 0, 1}},
            {{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}},
            {{1, 1, 0}, {1, 0, 0}, {0, 0, 0}, {0, 1, 0}},
            {{0, 1, 1}, {0, 0, 1}, {1, 0, 1}, {1, 1, 1}},
            {{0, 1, 0}, {0, 0, 0}, {0, 0, 1}, {0, 1, 1}},
            {{1, 1, 1}, {1, 0, 1}, {1, 0, 0}, {1, 1, 0}}
    };

    private static final int[][] BED_DIRECTION = {{1, 0, 3, 2, 5, 4}, {1, 0, 5, 4, 2, 3}, {1, 0, 2, 3, 4, 5}, {1, 0, 4, 5, 3, 2}};
    private static final int[] HEAD_INVISIBLE_FACE = {3, 4, 2, 5};
    private static final int[] FOOT_INVISIBLE_FACE_REMAP = {2, 3, 0, 1};
    static final String[] BED_FACING = {"south", "west", "north", "east"};

    interface Mapping {
        double[] uv(double x, double y, double z);
    }

    private BetaBlockModels() {
    }

    static String face(int side, double[] from, double[] to, Mapping beta, String texture, String cullface) {
        double[][] wanted = new double[4][];
        for (int i = 0; i < 4; i++) {
            int[] corner = CORNERS[side][i];
            wanted[i] = beta.uv(corner[0] == 0 ? from[0] : to[0], corner[1] == 0 ? from[1] : to[1],
                    corner[2] == 0 ? from[2] : to[2]);
        }
        for (int shift = 0; shift < 4; shift++) {
            double[] first = wanted[Math.floorMod(-shift, 4)];
            double[] third = wanted[Math.floorMod(2 - shift, 4)];
            double minU = first[0];
            double minV = first[1];
            double maxU = third[0];
            double maxV = third[1];
            double[][] pattern = {{minU, minV}, {minU, maxV}, {maxU, maxV}, {maxU, minV}};
            boolean fits = true;
            for (int i = 0; i < 4 && fits; i++) {
                double[] got = pattern[(i + shift) % 4];
                fits = Math.abs(got[0] - wanted[i][0]) < 1.0E-6 && Math.abs(got[1] - wanted[i][1]) < 1.0E-6;
            }
            if (fits) {
                StringBuilder json = new StringBuilder();
                json.append('"').append(FACES[side]).append("\": {\"texture\": \"#").append(texture)
                        .append("\", \"uv\": [").append(num(minU)).append(", ").append(num(minV)).append(", ")
                        .append(num(maxU)).append(", ").append(num(maxV)).append(']');
                if (shift != 0) {
                    json.append(", \"rotation\": ").append(shift * 90);
                }
                if (cullface != null) {
                    json.append(", \"cullface\": \"").append(cullface).append('"');
                }
                return json.append('}').toString();
            }
        }
        throw new IllegalStateException("no rectangle fits Beta's coordinates on face " + FACES[side]);
    }

    static Mapping standard(int side, boolean flip) {
        return (x, y, z) -> {
            double u;
            double v;
            switch (side) {
                case 0, 1 -> {
                    u = x;
                    v = z;
                }
                case 2 -> {
                    u = 16.0D - x;
                    v = 16.0D - y;
                }
                case 3 -> {
                    u = x;
                    v = 16.0D - y;
                }
                case 4 -> {
                    u = z;
                    v = 16.0D - y;
                }
                default -> {
                    u = 16.0D - z;
                    v = 16.0D - y;
                }
            }
            return new double[]{flip ? 16.0D - u : u, v};
        };
    }

    static String cube(String[] textures, boolean ambientOcclusion, String extra) {
        double[] from = {0, 0, 0};
        double[] to = {16, 16, 16};
        List<String> faces = new ArrayList<>();
        for (int side = 0; side < 6; side++) {
            faces.add(face(side, from, to, standard(side, false), "s" + side, FACES[side]));
        }
        StringBuilder json = new StringBuilder("{\n  \"parent\": \"minecraft:block/block\",\n");
        if (!ambientOcclusion) {
            json.append("  \"ambientocclusion\": false,\n");
        }
        json.append("  \"textures\": {\"particle\": \"").append(textures[6] != null ? textures[6] : textures[0]).append('"');
        for (int side = 0; side < 6; side++) {
            json.append(", \"s").append(side).append("\": \"").append(textures[side]).append('"');
        }
        json.append("},\n");
        if (extra != null) {
            json.append(extra);
        }
        json.append("  \"elements\": [{\"from\": [0, 0, 0], \"to\": [16, 16, 16], \"faces\": {\n    ")
                .append(String.join(",\n    ", faces)).append("\n  }}]\n}\n");
        return json.toString();
    }

    static String bed(int dir, boolean head, String[] tiles) {
        int skip = head ? HEAD_INVISIBLE_FACE[FOOT_INVISIBLE_FACE_REMAP[dir]] : HEAD_INVISIBLE_FACE[dir];
        int flip = switch (dir) {
            case 0 -> 5;
            case 1 -> 3;
            case 3 -> 2;
            default -> 4;
        };
        double[] box0 = {0, 0, 0};
        double[] box1 = {16, 9, 16};
        List<String> mainFaces = new ArrayList<>();
        mainFaces.add(face(1, box0, box1, bedTop(dir), "top", null));
        for (int side = 2; side < 6; side++) {
            if (side == skip) {
                continue;
            }
            int rel = BED_DIRECTION[dir][side];
            String texture;
            if (head) {
                texture = rel == 2 ? "head_end" : rel == 4 || rel == 5 ? "head_side" : "head_top";
            } else {
                texture = rel == 3 ? "foot_end" : rel == 4 || rel == 5 ? "foot_side" : "foot_top";
            }
            mainFaces.add(face(side, box0, box1, standard(side, side == flip), texture, FACES[side]));
        }
        String bottom = face(0, new double[]{0, 3, 0}, new double[]{16, 3, 16}, standard(0, false), "bottom", null);
        String top = head ? "head_top" : "foot_top";
        return "{\n  \"parent\": \"minecraft:block/block\",\n  \"ambientocclusion\": false,\n"
                + "  \"textures\": {\"particle\": \"" + tiles[6] + "\", \"bottom\": \"" + tiles[6]
                + "\", \"top\": \"" + (head ? tiles[1] : tiles[0]) + "\", \"foot_top\": \"" + tiles[0]
                + "\", \"head_top\": \"" + tiles[1] + "\", \"foot_end\": \"" + tiles[2] + "\", \"foot_side\": \""
                + tiles[3] + "\", \"head_side\": \"" + tiles[4] + "\", \"head_end\": \"" + tiles[5] + "\"},\n"
                + "  \"elements\": [\n"
                + "    {\"from\": [0, 3, 0], \"to\": [16, 3, 16], \"faces\": {" + bottom + "}},\n"
                + "    {\"from\": [0, 0, 0], \"to\": [16, 9, 16], \"faces\": {\n      "
                + String.join(",\n      ", mainFaces) + "\n    }}\n  ]\n}\n";
    }

    private static Mapping bedTop(int dir) {
        int[][] corners = switch (dir) {
            case 0 -> new int[][]{{16, 16}, {0, 16}, {0, 0}, {16, 0}};
            case 1 -> new int[][]{{0, 16}, {0, 0}, {16, 0}, {16, 16}};
            case 2 -> new int[][]{{0, 0}, {16, 0}, {16, 16}, {0, 16}};
            default -> new int[][]{{16, 0}, {16, 16}, {0, 16}, {0, 0}};
        };
        return (x, y, z) -> {
            int index = x > 8 ? (z > 8 ? 0 : 1) : (z > 8 ? 3 : 2);
            return new double[]{corners[index][0], corners[index][1]};
        };
    }

    static List<String[]> chestVariants() {
        List<String[]> variants = new ArrayList<>();
        String top = "top";
        String side = "side";
        for (int front = 2; front < 6; front++) {
            String[] faces = {top, top, side, side, side, side};
            faces[front] = "front";
            variants.add(variant("single_" + FACES[front], faces));
        }
        for (boolean partnerNorth : new boolean[]{true, false}) {
            for (int front : new int[]{5, 4}) {
                String[] faces = {top, top, side, side, null, null};
                for (int face : new int[]{4, 5}) {
                    int off = partnerNorth ? -1 : 0;
                    if (face == 4) {
                        off = -1 - off;
                    }
                    faces[face] = (face == front ? "front_" : "back_") + (off == -1 ? "a" : "b");
                }
                variants.add(variant("z_" + (partnerNorth ? "north" : "south") + "_" + FACES[front], faces));
            }
        }
        for (boolean partnerWest : new boolean[]{true, false}) {
            for (int front : new int[]{3, 2}) {
                String[] faces = {top, top, null, null, side, side};
                for (int face : new int[]{2, 3}) {
                    int off = partnerWest ? -1 : 0;
                    if (face == 3) {
                        off = -1 - off;
                    }
                    faces[face] = (face == front ? "front_" : "back_") + (off == -1 ? "a" : "b");
                }
                variants.add(variant("x_" + (partnerWest ? "west" : "east") + "_" + FACES[front], faces));
            }
        }
        return variants;
    }

    private static String[] variant(String name, String[] faces) {
        String[] out = new String[7];
        out[0] = name;
        System.arraycopy(faces, 0, out, 1, 6);
        return out;
    }

    static String num(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%s", value);
    }
}
