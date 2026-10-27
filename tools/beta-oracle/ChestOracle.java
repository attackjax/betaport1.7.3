import java.io.PrintWriter;
import java.util.Random;

public final class ChestOracle {
    static final int[] PALETTE = {0, 54, 1, 20, 18, 3, 50, 46, 44, 54};
    static final int CASES = 4000;

    private ChestOracle() {
    }

    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        try (PrintWriter out = new PrintWriter(args[0], "UTF-8")) {
            for (int n = 0; n < CASES; n++) {
                int[] patch = patch(n);
                xp world = (xp) java.lang.reflect.Proxy.newProxyInstance(xp.class.getClassLoader(),
                        new Class<?>[] {xp.class}, (proxy, method, a) -> {
                            if (method.getName().equals("a") && a != null && a.length == 3) {
                                return id(patch, (Integer) a[0], (Integer) a[1], (Integer) a[2]);
                            }
                            Class<?> type = method.getReturnType();
                            if (type == boolean.class) {
                                return false;
                            }
                            if (type == int.class) {
                                return 0;
                            }
                            if (type == float.class) {
                                return 0.0F;
                            }
                            return null;
                        });
                StringBuilder line = new StringBuilder(Integer.toString(n));
                for (int side = 0; side < 6; side++) {
                    line.append(' ').append(uu.m[54].a(world, 2, 0, 2, side));
                }
                out.println(line);
            }
        }
    }

    static int[] patch(int n) {
        Random random = new Random(n);
        int[] patch = new int[25];
        for (int i = 0; i < 25; i++) {
            patch[i] = PALETTE[random.nextInt(10)];
        }
        patch[2 * 5 + 2] = 54;
        return patch;
    }

    static int id(int[] patch, int x, int y, int z) {
        if (y != 0 || x < 0 || x > 4 || z < 0 || z > 4) {
            return 0;
        }
        return patch[x * 5 + z];
    }
}
