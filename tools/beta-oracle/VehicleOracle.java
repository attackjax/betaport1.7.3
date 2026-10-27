import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class VehicleOracle {
    static fd world;
    static final Map<Long, lm> chunks = new HashMap<>();

    public static void main(String[] args) {
        final ei info = new ei(0L, "oracle");
        wt saves = new wt() {
            public ei c() {
                return info;
            }

            public void b() {
            }

            public bf a(xa provider) {
                return new bf() {
                    public lm a(fd w, int x, int z) {
                        return null;
                    }

                    public void a(fd w, lm chunk) {
                    }

                    public void b(fd w, lm chunk) {
                    }

                    public void a() {
                    }

                    public void b() {
                    }
                };
            }

            public void a(ei worldInfo, List players) {
            }

            public void a(ei worldInfo) {
            }

            public File a(String name) {
                return null;
            }
        };
        world = new fd(saves, "oracle", 0L);
        world.r = new Random(1L);
        world.v = new cl() {
            public boolean a(int x, int z) {
                return true;
            }

            public lm b(int x, int z) {
                return chunk(x, z);
            }

            public lm c(int x, int z) {
                return chunk(x, z);
            }

            public void a(cl self, int x, int z) {
            }

            public boolean a(boolean all, yb progress) {
                return true;
            }

            public boolean a() {
                return false;
            }

            public boolean b() {
                return false;
            }

            public String c() {
                return "VehicleOracle";
            }
        };
        for (String which : args) {
            run(which);
        }
    }

    static lm chunk(int cx, int cz) {
        long key = (long) cx << 32 ^ (cz & 0xFFFFFFFFL);
        lm c = chunks.get(key);
        if (c == null) {
            byte[] blocks = new byte[32768];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y <= 100; y++) {
                        blocks[x << 11 | z << 7 | y] = 1;
                    }
                }
            }
            c = new lm(world, blocks, cx, cz);
            chunks.put(key, c);
        }
        return c;
    }

    static void set(int x, int y, int z, int id, int meta) {
        lm c = chunk(x >> 4, z >> 4);
        c.b[(x & 15) << 11 | (z & 15) << 7 | y] = (byte) id;
        c.e.a(x & 15, y, z & 15, meta);
    }

    static final int STONE = 1, RAIL = 66, POWERED = 27, WATER = 9;

    static ls rider(float forward, float strafe, float yaw, boolean holdYaw, boolean player) {
        ls r = new ls(world) {
            protected void f_() {
                this.aw = strafe;
                this.ax = forward;
                if (holdYaw) {
                    this.aS = yaw;
                }
            }

            public double I() {
                return player ? (double) (this.bf - 0.5F) : super.I();
            }
        };
        if (player) {
            r.bf = 1.62F;
        }
        r.c(0.0D, 0.0D, 0.0D, yaw, 0.0F);
        return r;
    }

    static void mount(sn rider, sn vehicle) {
        rider.aH = vehicle;
        vehicle.aG = rider;
    }

    static yl cart(double x, double y, double z, int type, double mx, double mz) {
        yl c = new yl(world, x, y, z, type);
        c.aP = mx;
        c.aR = mz;
        world.b(c);
        return c;
    }

    static String cartLine(String tag, int t, yl c) {
        return String.format("OR %s %d x=%.6f y=%.6f z=%.6f mx=%.6f my=%.6f mz=%.6f yaw=%.4f%s", tag, t, c.aM, c.aN, c.aO,
                c.aP, c.aQ, c.aR, c.aS, c.aG != null ? String.format(" ryaw=%.4f", c.aG.aS) : "");
    }

    static String boatLine(String tag, int t, fz b) {
        return String.format("OR %s %d x=%.6f y=%.6f z=%.6f mx=%.6f my=%.6f mz=%.6f yaw=%.4f dead=%b", tag, t, b.aM, b.aN, b.aO,
                b.aP, b.aQ, b.aR, b.aS, b.be);
    }

    static void tick(List<? extends sn> list) {
        for (sn e : list) {
            if (!e.be && e.aH == null) {
                world.f(e);
            }
        }
    }

    static void run(String which) {
        switch (which) {
            case "cart_flat" -> {
                for (int x = 605; x <= 655; x++) set(x, 101, 620, RAIL, 1);
                yl c = cart(606.5, 101.0, 620.5, 0, 0.5, 0.0);
                for (int t = 1; t <= 100; t++) {
                    tick(List.of(c));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "cart_rider" -> {
                for (int x = 605; x <= 655; x++) set(x, 101, 624, RAIL, 1);
                yl c = cart(606.5, 101.0, 624.5, 0, 0.5, 0.0);
                ls r = rider(0.0F, 0.0F, 0.0F, false, false);
                world.b(r);
                mount(r, c);
                for (int t = 1; t <= 100; t++) {
                    tick(List.of(c));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "cart_slope" -> {
                for (int x = 605; x <= 619; x++) set(x, 101, 628, RAIL, 1);
                set(620, 101, 628, RAIL, 2);
                for (int x = 621; x <= 640; x++) {
                    set(x, 101, 628, STONE, 0);
                    set(x, 102, 628, RAIL, 1);
                }
                yl c = cart(616.5, 101.0, 628.5, 0, 0.4, 0.0);
                for (int t = 1; t <= 120; t++) {
                    tick(List.of(c));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "cart_powered" -> {
                set(609, 101, 632, STONE, 0);
                for (int x = 610; x <= 614; x++) set(x, 101, 632, POWERED, 1 | 8);
                for (int x = 615; x <= 655; x++) set(x, 101, 632, RAIL, 1);
                yl c = cart(610.5, 101.0, 632.5, 0, 0.0, 0.0);
                for (int t = 1; t <= 80; t++) {
                    tick(List.of(c));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "cart_brake" -> {
                for (int x = 605; x <= 619; x++) set(x, 101, 636, RAIL, 1);
                for (int x = 620; x <= 624; x++) set(x, 101, 636, POWERED, 1);
                for (int x = 625; x <= 640; x++) set(x, 101, 636, RAIL, 1);
                yl c = cart(616.5, 101.0, 636.5, 0, 0.4, 0.0);
                for (int t = 1; t <= 80; t++) {
                    tick(List.of(c));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "cart_curve" -> {
                for (int z = 605; z <= 614; z++) set(650, 101, z, RAIL, 0);
                set(650, 101, 615, RAIL, 9);
                for (int x = 651; x <= 658; x++) set(x, 101, 615, RAIL, 1);
                yl c = cart(650.5, 101.0, 606.5, 0, 0.0, 0.3);
                ls r = rider(0.0F, 0.0F, 0.0F, false, false);
                world.b(r);
                mount(r, c);
                for (int t = 1; t <= 60; t++) {
                    tick(List.of(c));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "furnace" -> {
                for (int x = 605; x <= 655; x++) set(x, 101, 638, RAIL, 1);
                yl c = cart(606.5, 101.0, 638.5, 2, 0.0, 0.0);
                c.e = 1200;
                c.f = 1.0;
                c.g = 0.0;
                for (int t = 1; t <= 120; t++) {
                    tick(List.of(c));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "cart_bump_ew" -> {
                for (int x = 605; x <= 655; x++) set(x, 101, 602, RAIL, 1);
                yl a = cart(606.5, 101.0, 602.5, 0, 0.3, 0.0);
                yl b = cart(609.5, 101.0, 602.5, 0, 0.0, 0.0);
                for (int t = 1; t <= 70; t++) {
                    tick(List.of(a, b));
                    System.out.println(cartLine(which + "_a", t, a));
                    System.out.println(cartLine(which + "_b", t, b));
                }
            }
            case "cart_bump_ns" -> {
                for (int z = 600; z <= 640; z++) set(656, 101, z, RAIL, 0);
                yl a = cart(656.5, 101.0, 601.5, 0, 0.0, 0.3);
                yl b = cart(656.5, 101.0, 604.5, 0, 0.0, 0.0);
                for (int t = 1; t <= 70; t++) {
                    tick(List.of(a, b));
                    System.out.println(cartLine(which + "_a", t, a));
                    System.out.println(cartLine(which + "_b", t, b));
                }
            }
            case "cart_pickup" -> {
                for (int x = 605; x <= 655; x++) set(x, 101, 618, RAIL, 1);
                yl c = cart(606.5, 101.0, 618.5, 0, 0.4, 0.0);
                ls stand = rider(0.0F, 0.0F, 0.0F, false, false);
                stand.c(611.5, 101.0, 618.5, 0.0F, 0.0F);
                world.b(stand);
                for (int t = 1; t <= 60; t++) {
                    tick(List.of(c, stand));
                    System.out.println(cartLine(which, t, c));
                }
            }
            case "boat_float", "boat_row", "boat_crash", "boat_wall" -> {
                for (int x = 604; x <= 616; x++) {
                    for (int z = 604; z <= 614; z++) {
                        for (int y = 98; y <= 100; y++) set(x, y, z, WATER, 0);
                    }
                }
                fz b;
                List<hl> before = items();
                switch (which) {
                    case "boat_float" -> {
                        b = new fz(world, 610.5, 102.0, 609.5);
                        world.b(b);
                    }
                    case "boat_row" -> {
                        b = new fz(world, 605.5, 100.9, 609.5);
                        world.b(b);
                        ls r = rider(1.0F, 0.0F, -90.0F, true, true);
                        r.c(605.5, 100.4, 609.5, -90.0F, 0.0F);
                        world.b(r);
                        mount(r, b);
                    }
                    case "boat_crash" -> {
                        b = new fz(world, 610.5, 100.9, 610.5);
                        b.aP = 0.25;
                        b.aR = 0.25;
                        world.b(b);
                    }
                    default -> {
                        b = new fz(world, 612.5, 100.9, 609.5);
                        b.aP = 0.3;
                        world.b(b);
                    }
                }
                int n = which.equals("boat_float") ? 200 : 100;
                for (int t = 1; t <= n; t++) {
                    tick(List.of(b));
                    if (b.aG != null && b.aG.aH == b) {
                    }
                    System.out.println(boatLine(which, t, b));
                    if (b.be) {
                        List<hl> now = items();
                        now.removeAll(before);
                        StringBuilder drops = new StringBuilder();
                        for (hl item : now) drops.append(' ').append(item.a.c).append('x').append(item.a.a);
                        System.out.println("OR " + which + " drops" + drops);
                        break;
                    }
                }
            }
            default -> System.out.println("OR unknown " + which);
        }
    }

    static List<hl> items() {
        List<hl> list = new ArrayList<>();
        for (Object o : world.b) {
            if (o instanceof hl item) list.add(item);
        }
        return list;
    }
}
