import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class RecipeOracle {
    private static final Map<String, String> NAMES = new TreeMap<>();

    private RecipeOracle() {
    }

    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        Field damage = iz.class.getDeclaredField("d");
        damage.setAccessible(true);
        try (PrintWriter out = new PrintWriter(args[0], "UTF-8")) {
            List<?> recipes = hk.a().b();
            for (Object recipe : recipes) {
                if (recipe instanceof is shaped) {
                    int width = field(is.class, "b", shaped);
                    int height = field(is.class, "c", shaped);
                    iz[] cells = fieldObject(is.class, "d", shaped);
                    iz result = fieldObject(is.class, "e", shaped);
                    StringBuilder line = new StringBuilder("shaped " + width + " " + height + " "
                            + output(result, damage));
                    for (iz cell : cells) {
                        line.append(' ').append(cell == null ? "-" : item(cell, damage));
                    }
                    out.println(line);
                } else if (recipe instanceof tt shapeless) {
                    iz result = fieldObject(tt.class, "a", shapeless);
                    List<?> ingredients = fieldObject(tt.class, "b", shapeless);
                    StringBuilder line = new StringBuilder("shapeless " + output(result, damage));
                    for (Object ingredient : ingredients) {
                        line.append(' ').append(item((iz) ingredient, damage));
                    }
                    out.println(line);
                } else {
                    out.println("other " + recipe.getClass().getName());
                }
            }
            Map<?, ?> smelting = ey.a().b();
            for (Map.Entry<?, ?> entry : new TreeMap<>(smelting).entrySet()) {
                out.println("smelt " + entry.getKey() + " " + output((iz) entry.getValue(), damage));
            }
            sk furnace = new sk();
            Method burnTime = sk.class.getDeclaredMethod("a", iz.class);
            burnTime.setAccessible(true);
            for (int id = 0; id < gm.c.length; id++) {
                if (gm.c[id] == null) {
                    continue;
                }
                int ticks = (Integer) burnTime.invoke(furnace, new iz(id, 1, 0));
                if (ticks > 0) {
                    out.println("fuel " + id + " " + ticks);
                    name(new iz(id, 1, 0), damage);
                }
            }
            for (Map.Entry<String, String> entry : NAMES.entrySet()) {
                out.println("name " + entry.getKey() + " " + entry.getValue());
            }
            System.out.println(recipes.size() + " crafting recipes, " + smelting.size() + " smelting");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T fieldObject(Class<?> type, String name, Object owner) throws ReflectiveOperationException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(owner);
    }

    private static int field(Class<?> type, String name, Object owner) throws ReflectiveOperationException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(owner);
    }

    private static String item(iz stack, Field damage) throws ReflectiveOperationException {
        name(stack, damage);
        return stack.c + ":" + damage.getInt(stack);
    }

    private static String output(iz stack, Field damage) throws ReflectiveOperationException {
        return item(stack, damage) + "*" + stack.a;
    }

    private static void name(iz stack, Field damage) throws ReflectiveOperationException {
        int d = damage.getInt(stack);
        String key = stack.c + ":" + d;
        if (NAMES.containsKey(key)) {
            return;
        }
        gm item = gm.c[stack.c];
        NAMES.put(key, item.a(d < 0 ? new iz(stack.c, 1, 0) : stack));
    }
}
