public class StackOracle {
    public static void main(String[] args) throws Exception {
        Class.forName("uu");
        gm[] items = gm.c;
        for (int id = 0; id < items.length; id++) {
            if (items[id] != null && items[id].d() != 64) {
                System.out.println(id + " " + items[id].a() + " " + items[id].d());
            }
        }
    }
}
