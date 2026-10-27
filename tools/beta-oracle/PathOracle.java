import java.lang.reflect.Field;
import java.util.Random;

public final class PathOracle {
    static final float[][] SIZES = {{0.6F, 1.8F}, {1.4F, 0.9F}, {0.9F, 0.9F}, {0.6F, 1.6F}, {0.3F, 0.7F}, {4.0F, 4.0F}};

    private PathOracle() {
    }

    static int[][][][] world(Random random) {
        int[][][] ids = new int[32][16][32];
        int[][][] metas = new int[32][16][32];
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                int ground = 3 + random.nextInt(2) + (random.nextInt(6) == 0 ? random.nextInt(4) : 0);
                for (int y = 0; y < 16; y++) {
                    if (y <= ground) {
                        ids[x][y][z] = random.nextInt(25) == 0 ? 0 : 1;
                    } else if (y <= ground + 2) {
                        int roll = random.nextInt(100);
                        ids[x][y][z] = roll < 12 ? 1 : roll < 16 ? 9 : roll < 18 ? 11 : roll < 21 ? 64 : roll < 22 ? 71
                                : roll < 24 ? 85 : 0;
                        metas[x][y][z] = random.nextInt(16);
                    }
                }
            }
        }
        return new int[][][][] {ids, metas};
    }

    public static void main(String[] args) throws Exception {
        int samples = Integer.parseInt(args[0]);
        Random random = new Random(Long.parseLong(args[1]));
        sun.misc.Unsafe unsafe;
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (sun.misc.Unsafe) f.get(null);
        long hash = 0xcbf29ce484222325L;
        int found = 0;
        int points = 0;
        for (int s = 0; s < samples; s++) {
            int[][][][] w = world(random);
            final int[][][] ids = w[0];
            final int[][][] metas = w[1];
            xp world = new xp() {
                private boolean in(int x, int y, int z) {
                    return x >= -16 && x < 16 && y >= 56 && y < 72 && z >= -16 && z < 16;
                }

                public int a(int x, int y, int z) {
                    return in(x, y, z) ? ids[x + 16][y - 56][z + 16] : 0;
                }

                public ow b(int x, int y, int z) {
                    return null;
                }

                public float a(int x, int y, int z, int m) {
                    return 1.0F;
                }

                public float c(int x, int y, int z) {
                    return 1.0F;
                }

                public int e(int x, int y, int z) {
                    return in(x, y, z) ? metas[x + 16][y - 56][z + 16] : 0;
                }

                public ln f(int x, int y, int z) {
                    return ln.a;
                }

                public boolean g(int x, int y, int z) {
                    return false;
                }

                public boolean h(int x, int y, int z) {
                    return false;
                }

                public xv a() {
                    return null;
                }
            };
            float[] size = SIZES[random.nextInt(SIZES.length)];
            double sx = random.nextInt(24) - 12 + random.nextDouble();
            double sy = 56 + 4 + random.nextInt(5);
            double sz = random.nextInt(24) - 12 + random.nextDouble();
            int tx = (int) Math.floor(sx) + random.nextInt(25) - 12;
            int ty = (int) sy + random.nextInt(7) - 3;
            int tz = (int) Math.floor(sz) + random.nextInt(25) - 12;
            float distance = random.nextBoolean() ? 16.0F : 10.0F;
            boolean toEntity = random.nextBoolean();
            sn mob = (sn) unsafe.allocateInstance(by.class);
            eq box = (eq) unsafe.allocateInstance(eq.class);
            box.a = sx - size[0] / 2.0F;
            box.b = sy;
            box.c = sz - size[0] / 2.0F;
            box.d = sx + size[0] / 2.0F;
            box.e = sy + size[1];
            box.f = sz + size[0] / 2.0F;
            setFinal(unsafe, sn.class.getField("aW"), mob, box);
            mob.bg = size[0];
            mob.bh = size[1];
            mob.aM = sx;
            mob.aN = sy;
            mob.aO = sz;
            dh path;
            if (toEntity) {
                sn target = (sn) unsafe.allocateInstance(by.class);
                eq tbox = (eq) unsafe.allocateInstance(eq.class);
                double ox = tx + random.nextDouble();
                double oz = tz + random.nextDouble();
                tbox.b = ty;
                setFinal(unsafe, sn.class.getField("aW"), target, tbox);
                target.aM = ox;
                target.aN = ty;
                target.aO = oz;
                path = new fw(world).a(mob, target, distance);
            } else {
                path = new fw(world).a(mob, tx, ty, tz, distance);
            }
            if (path == null) {
                hash = mix(hash, -1);
                continue;
            }
            found++;
            Field pointsField = dh.class.getDeclaredField("b");
            pointsField.setAccessible(true);
            d[] list = (d[]) pointsField.get(path);
            hash = mix(hash, list.length);
            for (d p : list) {
                hash = mix(hash, p.a);
                hash = mix(hash, p.b);
                hash = mix(hash, p.c);
                points++;
            }
        }
        System.out.println(samples + " searches, " + found + " paths, " + points + " points");
        System.out.printf("hash 0x%016x%n", hash);
    }

    private static void setFinal(sun.misc.Unsafe unsafe, Field field, Object target, Object value) {
        unsafe.putObject(target, unsafe.objectFieldOffset(field), value);
    }

    static long mix(long hash, int value) {
        for (int shift = 0; shift < 32; shift += 8) {
            hash = (hash ^ (value >>> shift & 255)) * 0x100000001b3L;
        }
        return hash;
    }
}
