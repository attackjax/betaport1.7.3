import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public final class SkyOracle {
    private SkyOracle() {
    }

    public static void main(String[] args) throws Exception {
        long seed = Long.parseLong(args[0]);
        final ei info = new ei(seed, "sky");
        wt saves = new wt() {
            public ei c() { return info; }
            public void b() { }
            public bf a(xa provider) { return null; }
            public void a(ei worldInfo, List players) { }
            public void a(ei worldInfo) { }
            public File a(String name) { return null; }
        };
        fd world = new fd(saves, "sky", seed);
        sn viewer = new sn(world) {
            protected void b() { }
            protected void a(nu tag) { }
            protected void b(nu tag) { }
        };
        Field[] weather = {fd.class.getDeclaredField("i"), fd.class.getDeclaredField("j"),
                fd.class.getDeclaredField("k"), fd.class.getDeclaredField("l")};
        for (Field f : weather) {
            f.setAccessible(true);
        }
        Random random = new Random(seed * 31L + 7L);
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < 3000; i++) {
            long time = random.nextInt(48000);
            float pt = random.nextFloat();
            for (Field f : weather) {
                f.setFloat(world, random.nextInt(3) == 0 ? random.nextFloat() : 0.0F);
            }
            world.n = random.nextInt(4) == 0 ? random.nextInt(3) : 0;
            world.x().a(time);
            viewer.aM = (double) (random.nextInt(400000) - 200000) + 0.5D;
            viewer.aO = (double) (random.nextInt(400000) - 200000) + 0.5D;
            float angle = world.b(pt);
            bt sky = world.a(viewer, pt);
            bt fog = world.d(pt);
            float[] glow = world.t.a(angle, pt);
            hash = mix(hash, Float.floatToIntBits((float) sky.a));
            hash = mix(hash, Float.floatToIntBits((float) sky.b));
            hash = mix(hash, Float.floatToIntBits((float) sky.c));
            hash = mix(hash, Float.floatToIntBits((float) fog.a));
            hash = mix(hash, Float.floatToIntBits((float) fog.b));
            hash = mix(hash, Float.floatToIntBits((float) fog.c));
            hash = mix(hash, glow == null ? -1 : Float.floatToIntBits(glow[0]) ^ Float.floatToIntBits(glow[1]) * 31
                    ^ Float.floatToIntBits(glow[2]) * 961 ^ Float.floatToIntBits(glow[3]) * 29791);
            hash = mix(hash, Float.floatToIntBits(world.e(pt)));
        }
        System.out.printf("sky checksum 0x%016x%n", hash);
        long clouds = 0xcbf29ce484222325L;
        for (int i = 0; i < 3000; i++) {
            long time = random.nextInt(48000);
            float pt = random.nextFloat();
            for (Field f : weather) {
                f.setFloat(world, random.nextInt(3) == 0 ? random.nextFloat() : 0.0F);
            }
            world.x().a(time);
            bt cloud = world.c(pt);
            clouds = mix(clouds, Float.floatToIntBits((float) cloud.a));
            clouds = mix(clouds, Float.floatToIntBits((float) cloud.b));
            clouds = mix(clouds, Float.floatToIntBits((float) cloud.c));
        }
        System.out.printf("cloud checksum 0x%016x%n", clouds);
        System.out.printf("star checksum 0x%016x%n", stars());
    }

    private static long stars() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        n renderGlobal = (n) unsafe.allocateInstance(n.class);
        Method build = n.class.getDeclaredMethod("g");
        build.setAccessible(true);
        try {
            build.invoke(renderGlobal);
        } catch (Throwable expected) {
        }
        Field raw = nw.class.getDeclaredField("g");
        Field count = nw.class.getDeclaredField("h");
        raw.setAccessible(true);
        count.setAccessible(true);
        int[] buffer = (int[]) raw.get(nw.a);
        int vertices = count.getInt(nw.a);
        int[] order = {0, 1, 2, 5};
        long hash = 0xcbf29ce484222325L;
        for (int square = 0; square < vertices / 6; square++) {
            for (int corner : order) {
                for (int c = 0; c < 3; c++) {
                    hash = mix(hash, buffer[(square * 6 + corner) * 8 + c]);
                }
            }
        }
        return hash;
    }

    private static long mix(long hash, int value) {
        for (int shift = 0; shift < 32; shift += 8) {
            hash = (hash ^ (value >>> shift & 255)) * 0x100000001b3L;
        }
        return hash;
    }
}
