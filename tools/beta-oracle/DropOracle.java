import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public final class DropOracle {
    private static final int TRIES = 20000;
    private static final int[] TOOLS = {269, 270, 271, 268, 273, 274, 275, 272, 256, 257, 258, 267,
            284, 285, 286, 283, 277, 278, 279, 276, 359};

    private DropOracle() {
    }

    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        Method damage = uu.class.getDeclaredMethod("b_", int.class);
        damage.setAccessible(true);
        Field material = uu.class.getDeclaredField("bA");
        material.setAccessible(true);
        try (PrintWriter out = new PrintWriter(args[0], "UTF-8")) {
            for (int id = 1; id < uu.m.length; id++) {
                uu block = uu.m[id];
                if (block == null) {
                    continue;
                }
                for (int meta : metas(id)) {
                    out.println(drops(block, id, meta, damage));
                }
            }
            for (int id = 1; id < uu.m.length; id++) {
                uu block = uu.m[id];
                if (block == null) {
                    continue;
                }
                boolean hand = ((ln) material.get(block)).i();
                StringBuilder line = new StringBuilder("harvest " + id);
                if (hand) {
                    line.append(" 0");
                }
                for (int tool : TOOLS) {
                    if (hand || new iz(tool, 1, 0).b(block)) {
                        line.append(' ').append(tool);
                    }
                }
                out.println(line);
            }
        }
    }

    private static int[] metas(int id) {
        return switch (id) {
            case 6, 17, 18, 31 -> new int[] {0, 1, 2};
            case 35 -> new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
            case 43, 44 -> new int[] {0, 1, 2, 3};
            case 59 -> new int[] {0, 1, 2, 3, 4, 5, 6, 7};
            case 26, 64, 71 -> new int[] {0, 8};
            default -> new int[] {0};
        };
    }

    private static String drops(uu block, int id, int meta, Method damage) throws ReflectiveOperationException {
        Random random = new Random(id * 16L + meta);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Map<Long, Integer> items = new TreeMap<>();
        for (int i = 0; i < TRIES; i++) {
            int count = block.a(random);
            int dropped = 0;
            for (int j = 0; j < count; j++) {
                int item = block.a(meta, random);
                if (item > 0) {
                    dropped++;
                    items.merge((long) item << 8 | (Integer) damage.invoke(block, meta), 1, Integer::sum);
                }
            }
            min = Math.min(min, dropped);
            max = Math.max(max, dropped);
        }
        StringBuilder line = new StringBuilder("drop " + id + ":" + meta + " " + min + "-" + max);
        for (Map.Entry<Long, Integer> item : items.entrySet()) {
            line.append(' ').append(item.getKey() >> 8).append(':').append(item.getKey() & 255).append('=')
                    .append(String.format("%.3f", item.getValue() / (double) TRIES));
        }
        return line.toString();
    }
}
