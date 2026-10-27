import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ModelOracle {
    private ModelOracle() {
    }

    private record Spec(String className, Object[] args, String parts) {
    }

    private static Spec spec(String model) {
        String quadruped = "d,e,f,g,h,i";
        String biped = "a,c,d,e,f,g,b";
        return switch (model) {
            case "pig" -> new Spec("eh", new Object[0], quadruped);
            case "pig_saddle" -> new Spec("eh", new Object[]{0.5F}, quadruped);
            case "cow" -> new Spec("hh", new Object[0], quadruped + ",b,c,a");
            case "sheep" -> new Spec("mw", new Object[0], quadruped);
            case "sheep_wool" -> new Spec("ea", new Object[0], quadruped);
            case "creeper" -> new Spec("il", new Object[0], "a,c,d,e,f,g");
            case "creeper_armor" -> new Spec("il", new Object[]{2.0F}, "a,c,d,e,f,g");
            case "ghast" -> new Spec("lo", new Object[0], "a,b[]");
            case "spider" -> new Spec("sc", new Object[0], "a,b,c,d,e,f,g,h,i,j,k");
            case "biped" -> new Spec("fh", new Object[]{0.0F}, biped);
            case "armor_outer" -> new Spec("fh", new Object[]{1.0F}, biped);
            case "armor_inner" -> new Spec("fh", new Object[]{0.5F}, biped);
            case "armor_head" -> new Spec("fh", new Object[]{1.0F}, "a,b");
            case "armor_chest" -> new Spec("fh", new Object[]{1.0F}, "c,d,e");
            case "armor_legs" -> new Spec("fh", new Object[]{0.5F}, "c,f,g");
            case "armor_feet" -> new Spec("fh", new Object[]{1.0F}, "f,g");
            case "pigzombie", "giant" -> new Spec("ej", new Object[0], biped);
            case "zombie" -> new Spec("ej", new Object[0], biped);
            case "skeleton" -> new Spec("lc", new Object[0], biped);
            case "chicken" -> new Spec("td", new Object[0], "a,g,h,b,c,d,e,f");
            case "squid" -> new Spec("wn", new Object[0], "a,b[]");
            case "slime" -> new Spec("no", new Object[]{16}, "a,b,c,d");
            case "slime_outer" -> new Spec("no", new Object[]{0}, "a");
            case "wolf" -> new Spec("o", new Object[0], "w:a,b,c,d,e,f,w:g,w:h,w:i,w:j,k");
            case "minecart" -> new Spec("nq", new Object[0], "a[]");
            case "boat" -> new Spec("ez", new Object[0], "a[]");
            default -> throw new IllegalArgumentException("unknown model " + model);
        };
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        String model = args[0];
        float[] pose = new float[5];
        for (int i = 0; i < 5 && i + 1 < args.length; i++) {
            pose[i] = Float.parseFloat(args[i + 1]);
        }
        Spec spec = spec(model);
        Class<?> type = Class.forName(spec.className);
        Object instance = construct(type, spec.args);
        Method setPose = type.getMethod("b", float.class, float.class, float.class, float.class, float.class, float.class);
        String flags = args.length > 6 ? args[6] : "";
        for (String flag : flags.split("\\+")) {
            if (flag.startsWith("swing=")) {
                setFloat(instance, "m", Float.parseFloat(flag.substring(6)));
            } else if (flag.equals("ride")) {
                find(type, "n").setBoolean(instance, true);
            } else if (flag.equals("sneak")) {
                find(type, "l").setBoolean(instance, true);
            } else if (flag.equals("item")) {
                find(type, "k").setBoolean(instance, true);
            }
        }
        if (model.equals("wolf")) {
            standingWolf(instance, pose);
            if (flags.contains("sit")) {
                sittingWolf(instance);
            }
        }
        setPose.invoke(instance, pose[0], pose[1], pose[2], pose[3], pose[4], 0.0625F);
        if (model.equals("minecart")) {
            Object[] parts = (Object[]) field(instance, "a");
            setFloat(parts[5], "b", 4.0F - -0.1F);
        }
        for (String entry : spec.parts.split(",")) {
            boolean yxz = entry.startsWith("w:");
            String name = yxz ? entry.substring(2) : entry;
            if (name.endsWith("[]")) {
                Object array = field(instance, name.substring(0, name.length() - 2));
                for (int i = 0; i < Array.getLength(array); i++) {
                    dump(name.substring(0, name.length() - 2) + i, Array.get(array, i), yxz);
                }
            } else {
                Object part = field(instance, name);
                if (part != null) {
                    dump(name, part, yxz);
                }
            }
        }
    }

    private static void standingWolf(Object wolf, float[] pose) throws ReflectiveOperationException {
        float f = pose[0];
        float f1 = pose[1];
        Object body = field(wolf, "b");
        Object mane = field(wolf, "k");
        Object tail = field(wolf, "j");
        setFloat(tail, "e", (float) Math.cos(f * 0.6662F) * 1.4F * f1);
        point(body, 0.0F, 14.0F, 2.0F);
        setFloat(body, "d", (float) (Math.PI / 2));
        point(mane, -1.0F, 14.0F, -3.0F);
        setFloat(mane, "d", getFloat(body, "d"));
        point(tail, -1.0F, 12.0F, 8.0F);
        String[] legs = {"c", "d", "e", "f"};
        float[][] points = {{-2.5F, 16.0F, 7.0F}, {0.5F, 16.0F, 7.0F}, {-2.5F, 16.0F, -4.0F}, {0.5F, 16.0F, -4.0F}};
        float[] phase = {0.0F, (float) Math.PI, (float) Math.PI, 0.0F};
        for (int i = 0; i < 4; i++) {
            Object leg = field(wolf, legs[i]);
            point(leg, points[i][0], points[i][1], points[i][2]);
            setFloat(leg, "d", (float) Math.cos(f * 0.6662F + phase[i]) * 1.4F * f1);
        }
    }

    private static void sittingWolf(Object wolf) throws ReflectiveOperationException {
        Object mane = field(wolf, "k");
        point(mane, -1.0F, 16.0F, -3.0F);
        setFloat(mane, "d", 1.2566371F);
        setFloat(mane, "e", 0.0F);
        Object body = field(wolf, "b");
        point(body, 0.0F, 18.0F, 0.0F);
        setFloat(body, "d", 0.7853982F);
        point(field(wolf, "j"), -1.0F, 21.0F, 6.0F);
        Object c = field(wolf, "c");
        point(c, -2.5F, 22.0F, 2.0F);
        setFloat(c, "d", 4.712389F);
        Object d = field(wolf, "d");
        point(d, 0.5F, 22.0F, 2.0F);
        setFloat(d, "d", 4.712389F);
        Object e = field(wolf, "e");
        setFloat(e, "d", 5.811947F);
        point(e, -2.49F, 17.0F, -4.0F);
        Object f = field(wolf, "f");
        setFloat(f, "d", 5.811947F);
        point(f, 0.51F, 17.0F, -4.0F);
    }

    private static Object construct(Class<?> type, Object[] args) throws ReflectiveOperationException {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] instanceof Integer ? int.class : float.class;
        }
        return type.getConstructor(types).newInstance(args);
    }

    private static void dump(String name, Object part, boolean yxz) throws ReflectiveOperationException {
        float px = getFloat(part, "a");
        float py = getFloat(part, "b");
        float pz = getFloat(part, "c");
        float rx = getFloat(part, "d");
        float ry = getFloat(part, "e");
        float rz = getFloat(part, "f");
        Object faces = field(part, "k");
        if (faces == null) {
            return;
        }
        for (int i = 0; i < Array.getLength(faces); i++) {
            Object quad = Array.get(faces, i);
            Object vertices = field(quad, "a");
            List<String> corners = new ArrayList<>();
            for (int v = 0; v < Array.getLength(vertices); v++) {
                Object vertex = Array.get(vertices, v);
                Object position = field(vertex, "a");
                double[] p = {getDouble(position, "a"), getDouble(position, "b"), getDouble(position, "c")};
                double[] r = yxz ? rotateYxz(p, rx, ry, rz) : rotateZyx(p, rx, ry, rz);
                float u = getFloat(vertex, "b") * 64.0F;
                float w = getFloat(vertex, "c") * 32.0F;
                corners.add(String.format(Locale.ROOT, "%.3f %.3f %.3f %s %s",
                        r[0] + px, r[1] + py, r[2] + pz, texel(u), texel(w)));
            }
            System.out.println("Q " + name + " | " + String.join(" | ", corners));
        }
    }

    private static String texel(float value) {
        float nearest = Math.round(value);
        return String.format(Locale.ROOT, "%.2f", Math.abs(value - nearest) < 0.15F ? nearest : value);
    }

    private static double[] rotateZyx(double[] p, float rx, float ry, float rz) {
        return rotZ(rotY(rotX(p, rx), ry), rz);
    }

    private static double[] rotateYxz(double[] p, float rx, float ry, float rz) {
        return rotY(rotX(rotZ(p, rz), rx), ry);
    }

    private static double[] rotX(double[] p, float a) {
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[]{p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c};
    }

    private static double[] rotY(double[] p, float a) {
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[]{p[0] * c + p[2] * s, p[1], -p[0] * s + p[2] * c};
    }

    private static double[] rotZ(double[] p, float a) {
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[]{p[0] * c - p[1] * s, p[0] * s + p[1] * c, p[2]};
    }

    private static void point(Object part, float x, float y, float z) throws ReflectiveOperationException {
        setFloat(part, "a", x);
        setFloat(part, "b", y);
        setFloat(part, "c", z);
    }

    private static Field find(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
            }
        }
        throw new NoSuchFieldException(type.getName() + "." + name);
    }

    private static Object field(Object owner, String name) throws ReflectiveOperationException {
        return find(owner.getClass(), name).get(owner);
    }

    private static float getFloat(Object owner, String name) throws ReflectiveOperationException {
        return find(owner.getClass(), name).getFloat(owner);
    }

    private static double getDouble(Object owner, String name) throws ReflectiveOperationException {
        return find(owner.getClass(), name).getDouble(owner);
    }

    private static void setFloat(Object owner, String name, float value) throws ReflectiveOperationException {
        find(owner.getClass(), name).setFloat(owner, value);
    }
}
