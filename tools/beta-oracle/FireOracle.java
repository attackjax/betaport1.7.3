public class FireOracle {
    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        java.lang.reflect.Field fa = yq.class.getDeclaredField("a");
        java.lang.reflect.Field fb = yq.class.getDeclaredField("b");
        fa.setAccessible(true);
        fb.setAccessible(true);
        int[] encourage = (int[]) fa.get(uu.as);
        int[] ability = (int[]) fb.get(uu.as);
        for (int id = 0; id < 256; id++) {
            if (encourage[id] != 0 || ability[id] != 0) {
                System.out.println("fire " + id + " " + encourage[id] + " " + ability[id]);
            }
        }
        StringBuilder burn = new StringBuilder("burns");
        StringBuilder solid = new StringBuilder("solid");
        StringBuilder moves = new StringBuilder("blocksMovement");
        for (int id = 1; id < 256; id++) {
            uu block = uu.m[id];
            if (block == null) {
                continue;
            }
            if (block.bA.e()) burn.append(' ').append(id);
            if (block.bA.a()) solid.append(' ').append(id);
            if (block.bA.c()) moves.append(' ').append(id);
        }
        System.out.println(burn);
        System.out.println(solid);
        System.out.println(moves);
    }
}
