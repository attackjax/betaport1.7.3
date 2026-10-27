package dev.b173.core.noise;

import dev.b173.core.BetaRandom;

final class SimplexNoise {
    private static final int[] GRAD_X = {1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0};
    private static final int[] GRAD_Y = {1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1};
    private static final double F2 = 0.5D * (Math.sqrt(3.0D) - 1.0D);
    private static final double G2 = (3.0D - Math.sqrt(3.0D)) / 6.0D;

    private final int[] p = new int[512];
    private final double xo;
    private final double yo;

    SimplexNoise(BetaRandom random) {
        xo = random.nextDouble() * 256.0D;
        yo = random.nextDouble() * 256.0D;
        random.nextDouble();
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i) + i;
            int t = p[i];
            p[i] = p[j];
            p[j] = t;
            p[i + 256] = p[i];
        }
    }

    private static int wrap(double value) {
        return value > 0.0D ? (int) value : (int) value - 1;
    }

    private static double dot(int g, double x, double y) {
        return (double) GRAD_X[g] * x + (double) GRAD_Y[g] * y;
    }

    void add(double[] out, double x, double y, int sizeX, int sizeY, double scaleX, double scaleY, double amplitude) {
        int index = 0;
        for (int ix = 0; ix < sizeX; ix++) {
            double px = (x + (double) ix) * scaleX + xo;
            for (int iy = 0; iy < sizeY; iy++) {
                double py = (y + (double) iy) * scaleY + yo;
                double s = (px + py) * F2;
                int i = wrap(px + s);
                int j = wrap(py + s);
                double t = (double) (i + j) * G2;
                double x0 = px - ((double) i - t);
                double y0 = py - ((double) j - t);
                int i1;
                int j1;
                if (x0 > y0) {
                    i1 = 1;
                    j1 = 0;
                } else {
                    i1 = 0;
                    j1 = 1;
                }
                double x1 = (x0 - (double) i1) + G2;
                double y1 = (y0 - (double) j1) + G2;
                double x2 = (x0 - 1.0D) + 2.0D * G2;
                double y2 = (y0 - 1.0D) + 2.0D * G2;
                int ii = i & 255;
                int jj = j & 255;
                int g0 = p[ii + p[jj]] % 12;
                int g1 = p[ii + i1 + p[jj + j1]] % 12;
                int g2 = p[ii + 1 + p[jj + 1]] % 12;
                double n0;
                double t0 = 0.5D - x0 * x0 - y0 * y0;
                if (t0 < 0.0D) {
                    n0 = 0.0D;
                } else {
                    t0 *= t0;
                    n0 = t0 * t0 * dot(g0, x0, y0);
                }
                double n1;
                double t1 = 0.5D - x1 * x1 - y1 * y1;
                if (t1 < 0.0D) {
                    n1 = 0.0D;
                } else {
                    t1 *= t1;
                    n1 = t1 * t1 * dot(g1, x1, y1);
                }
                double n2;
                double t2 = 0.5D - x2 * x2 - y2 * y2;
                if (t2 < 0.0D) {
                    n2 = 0.0D;
                } else {
                    t2 *= t2;
                    n2 = t2 * t2 * dot(g2, x2, y2);
                }
                out[index++] += 70.0D * (n0 + n1 + n2) * amplitude;
            }
        }
    }
}
