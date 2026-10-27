package dev.b173.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BetaPathFinder {
    public interface World {
        int id(int x, int y, int z);

        int meta(int x, int y, int z);
    }

    private static final BitSet BLOCKS = new BitSet(256);

    static {
        for (int id : new int[] {1, 2, 3, 4, 5, 7, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 29, 30,
                33, 34, 35, 36, 41, 42, 43, 44, 45, 46, 47, 48, 49, 52, 53, 54, 56, 57, 58, 60, 61, 62, 63, 64, 67, 68,
                70, 71, 72, 73, 74, 79, 80, 81, 82, 84, 85, 86, 87, 88, 89, 91, 92, 95, 96}) {
            BLOCKS.set(id);
        }
    }

    private static final class Point {
        final int x;
        final int y;
        final int z;
        int index = -1;
        float total;
        float toNext;
        float estimate;
        Point previous;
        boolean closed;

        Point(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        float distanceTo(Point other) {
            float dx = other.x - this.x;
            float dy = other.y - this.y;
            float dz = other.z - this.z;
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    private final World world;
    private final Map<Integer, Point> points = new HashMap<>();
    private Point[] heap = new Point[1024];
    private int count;
    private final Point[] options = new Point[32];

    public BetaPathFinder(World world) {
        this.world = world;
    }

    static int hash(int x, int y, int z) {
        return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 32768 : 0);
    }

    private Point open(int x, int y, int z) {
        int key = hash(x, y, z);
        Point point = this.points.get(key);
        if (point == null) {
            point = new Point(x, y, z);
            this.points.put(key, point);
        }
        return point;
    }

    private static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static int floor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public int[][] find(double minX, double minY, double minZ, float width, float height, double x, double y, double z,
                        float distance) {
        this.count = 0;
        this.points.clear();
        Point start = open(floor(minX), floor(minY), floor(minZ));
        Point end = open(floor(x - (double) (width / 2.0F)), floor(y), floor(z - (double) (width / 2.0F)));
        Point size = new Point(floor(width + 1.0F), floor(height + 1.0F), floor(width + 1.0F));
        return search(start, end, size, distance);
    }

    private int[][] search(Point start, Point end, Point size, float distance) {
        start.total = 0.0F;
        start.toNext = start.distanceTo(end);
        start.estimate = start.toNext;
        this.count = 0;
        add(start);
        Point closest = start;
        while (this.count != 0) {
            Point point = dequeue();
            if (point.x == end.x && point.y == end.y && point.z == end.z && hash(point.x, point.y, point.z)
                    == hash(end.x, end.y, end.z)) {
                return path(start, end);
            }
            if (point.distanceTo(end) < closest.distanceTo(end)) {
                closest = point;
            }
            point.closed = true;
            int found = options(point, size, end, distance);
            for (int i = 0; i < found; i++) {
                Point next = this.options[i];
                float total = point.total + point.distanceTo(next);
                if (next.index < 0 || total < next.total) {
                    next.previous = point;
                    next.total = total;
                    next.toNext = next.distanceTo(end);
                    if (next.index >= 0) {
                        change(next, next.total + next.toNext);
                    } else {
                        next.estimate = next.total + next.toNext;
                        add(next);
                    }
                }
            }
        }
        return closest == start ? null : path(start, closest);
    }

    private int options(Point point, Point size, Point end, float distance) {
        int found = 0;
        int jump = vertical(point.x, point.y + 1, point.z, size) == 1 ? 1 : 0;
        Point south = safe(point.x, point.y, point.z + 1, size, jump);
        Point west = safe(point.x - 1, point.y, point.z, size, jump);
        Point east = safe(point.x + 1, point.y, point.z, size, jump);
        Point north = safe(point.x, point.y, point.z - 1, size, jump);
        for (Point next : new Point[] {south, west, east, north}) {
            if (next != null && !next.closed && next.distanceTo(end) < distance) {
                this.options[found++] = next;
            }
        }
        return found;
    }

    private Point safe(int x, int y, int z, Point size, int jump) {
        Point point = null;
        if (vertical(x, y, z, size) == 1) {
            point = open(x, y, z);
        }
        if (point == null && jump > 0 && vertical(x, y + jump, z, size) == 1) {
            point = open(x, y + jump, z);
            y += jump;
        }
        if (point != null) {
            int fall = 0;
            int below = 0;
            while (y > 0 && (below = vertical(x, y - 1, z, size)) == 1) {
                fall++;
                if (fall >= 4) {
                    return null;
                }
                if (y > 0) {
                    point = open(x, y - 1, z);
                }
                y--;
            }
            if (below == -2) {
                return null;
            }
        }
        return point;
    }

    private int vertical(int x, int y, int z, Point size) {
        for (int i = x; i < x + size.x; i++) {
            for (int j = y; j < y + size.y; j++) {
                for (int k = z; k < z + size.z; k++) {
                    int id = this.world.id(i, j, k);
                    if (id <= 0) {
                        continue;
                    }
                    if (id == 64 || id == 71) {
                        if ((this.world.meta(i, j, k) & 4) == 0) {
                            return 0;
                        }
                    } else if (BLOCKS.get(id)) {
                        return 0;
                    } else if (id == 8 || id == 9) {
                        return -1;
                    } else if (id == 10 || id == 11) {
                        return -2;
                    }
                }
            }
        }
        return 1;
    }

    private static int[][] path(Point start, Point end) {
        List<int[]> reversed = new ArrayList<>();
        for (Point point = end; point != null; point = point.previous) {
            reversed.add(new int[] {point.x, point.y, point.z});
            if (point == start) {
                break;
            }
        }
        int[][] out = new int[reversed.size()][];
        for (int i = 0; i < out.length; i++) {
            out[i] = reversed.get(out.length - 1 - i);
        }
        return out;
    }

    private void add(Point point) {
        if (this.count == this.heap.length) {
            Point[] grown = new Point[this.count << 1];
            System.arraycopy(this.heap, 0, grown, 0, this.count);
            this.heap = grown;
        }
        this.heap[this.count] = point;
        point.index = this.count;
        up(this.count++);
    }

    private Point dequeue() {
        Point point = this.heap[0];
        this.heap[0] = this.heap[--this.count];
        this.heap[this.count] = null;
        if (this.count > 0) {
            down(0);
        }
        point.index = -1;
        return point;
    }

    private void change(Point point, float estimate) {
        float old = point.estimate;
        point.estimate = estimate;
        if (estimate < old) {
            up(point.index);
        } else {
            down(point.index);
        }
    }

    private void up(int i) {
        Point point = this.heap[i];
        float value = point.estimate;
        while (i > 0) {
            int parent = i - 1 >> 1;
            Point above = this.heap[parent];
            if (value >= above.estimate) {
                break;
            }
            this.heap[i] = above;
            above.index = i;
            i = parent;
        }
        this.heap[i] = point;
        point.index = i;
    }

    private void down(int i) {
        Point point = this.heap[i];
        float value = point.estimate;
        while (true) {
            int left = 1 + (i << 1);
            int right = left + 1;
            if (left >= this.count) {
                break;
            }
            Point l = this.heap[left];
            float lv = l.estimate;
            Point r;
            float rv;
            if (right >= this.count) {
                r = null;
                rv = Float.POSITIVE_INFINITY;
            } else {
                r = this.heap[right];
                rv = r.estimate;
            }
            if (lv < rv) {
                if (lv >= value) {
                    break;
                }
                this.heap[i] = l;
                l.index = i;
                i = left;
            } else {
                if (rv >= value) {
                    break;
                }
                this.heap[i] = r;
                r.index = i;
                i = right;
            }
        }
        this.heap[i] = point;
        point.index = i;
    }
}
