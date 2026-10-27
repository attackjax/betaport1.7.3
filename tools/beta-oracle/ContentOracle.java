import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

public final class ContentOracle {
    private ContentOracle() {
    }

    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        try (PrintWriter out = new PrintWriter(args[0], "UTF-8")) {
            for (int id = 1; id < uu.m.length; id++) {
                if (uu.m[id] != null) {
                    out.println("block " + id + " " + uu.m[id].n().trim());
                }
            }
            for (int id = 256; id < gm.c.length; id++) {
                if (gm.c[id] != null) {
                    out.println("item " + id + " " + gm.c[id].a(new iz(id, 1, 0)).trim());
                }
            }
            Map<Integer, String> entities = new TreeMap<>();
            Map<?, ?> idToClass = null;
            Map<?, ?> classToName = null;
            for (Field field : jc.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                Map<?, ?> map = (Map<?, ?>) field.get(null);
                Object key = map.keySet().iterator().next();
                Object value = map.values().iterator().next();
                if (key instanceof Integer && value instanceof Class) {
                    idToClass = map;
                } else if (key instanceof Class && value instanceof String) {
                    classToName = map;
                }
            }
            for (Map.Entry<?, ?> entry : idToClass.entrySet()) {
                entities.put((Integer) entry.getKey(), (String) classToName.get(entry.getValue()));
            }
            for (Map.Entry<Integer, String> entry : entities.entrySet()) {
                out.println("entity " + entry.getKey() + " " + entry.getValue());
            }
            Field material = uu.class.getDeclaredField("bA");
            material.setAccessible(true);
            StringBuilder opaque = new StringBuilder("opaque");
            StringBuilder normal = new StringBuilder("normal");
            for (int id = 1; id < 256; id++) {
                if (uu.o[id]) {
                    opaque.append(' ').append(id);
                }
                if (uu.m[id] != null && ((ln) material.get(uu.m[id])).h() && uu.m[id].d()) {
                    normal.append(' ').append(id);
                }
            }
            out.println(opaque);
            out.println(normal);
        }
    }
}
