public class SpeedOracle {
    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        int[] tools = {256, 257, 258, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 283, 284, 285,
                286, 290, 291, 292, 293, 294, 359};
        for (int tool : tools) {
            for (int id = 1; id < uu.m.length; id++) {
                if (uu.m[id] == null) continue;
                float s = new iz(gm.c[tool]).a(uu.m[id]);
                if (s != 1.0F) System.out.println(tool + " " + id + " " + s);
            }
        }
        java.lang.reflect.Field hardness = uu.class.getDeclaredField("bo");
        hardness.setAccessible(true);
        for (int id = 1; id < uu.m.length; id++) {
            if (uu.m[id] != null) System.out.println("hardness " + id + " " + hardness.get(uu.m[id]));
        }
    }
}
