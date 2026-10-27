import java.lang.reflect.Field;
import java.util.Random;

public final class TextureFxOracle {
    private TextureFxOracle() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        long seed = Long.parseLong(args[0]);
        int ticks = Integer.parseInt(args[1]);
        Class<?> holder = Class.forName("java.lang.Math$RandomNumberGeneratorHolder");
        Field field = holder.getDeclaredField("randomNumberGenerator");
        field.setAccessible(true);
        Random mathRandom = (Random) field.get(null);

        String[] names = {"lava_still", "lava_flow", "water_still", "water_flow", "fire_0", "fire_1", "portal"};
        for (String name : names) {
            mathRandom.setSeed(seed);
            aw effect = switch (name) {
                case "lava_still" -> new cg();
                case "lava_flow" -> (aw) Class.forName("if").getConstructor().newInstance();
                case "water_still" -> new vs();
                case "water_flow" -> new oh();
                case "fire_0" -> new sd(0);
                case "fire_1" -> new sd(1);
                default -> new hs();
            };
            long hash = 0xcbf29ce484222325L;
            for (int t = 0; t < ticks; t++) {
                effect.a();
                for (byte b : effect.a) {
                    hash = (hash ^ (b & 255)) * 0x100000001b3L;
                }
            }
            System.out.printf("%s: %d ticks from seed %d, checksum 0x%016xL%n", name, ticks, seed, hash);
        }
    }
}
