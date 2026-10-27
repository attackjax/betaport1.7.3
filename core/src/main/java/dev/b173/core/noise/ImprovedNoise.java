package dev.b173.core.noise;

import dev.b173.core.BetaRandom;

final class ImprovedNoise {
    private final int[] p = new int[512];
    private final double xo;
    private final double yo;
    private final double zo;

    ImprovedNoise(BetaRandom random) {
        xo = random.nextDouble() * 256.0D;
        yo = random.nextDouble() * 256.0D;
        zo = random.nextDouble() * 256.0D;
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

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        return switch (hash & 15) {
            case 0 -> x + y;
            case 1 -> -x + y;
            case 2 -> x - y;
            case 3 -> -x - y;
            case 4 -> x + z;
            case 5 -> -x + z;
            case 6 -> x - z;
            case 7 -> -x - z;
            case 8 -> y + z;
            case 9 -> -y + z;
            case 10 -> y - z;
            case 11 -> -y - z;
            case 12 -> y + x;
            case 13 -> -y + z;
            case 14 -> y - x;
            default -> -y - z;
        };
    }

    private static double grad2(int hash, double x, double z) {
        int h = hash & 15;
        double u = (double) (1 - ((h & 8) >> 3)) * x;
        double v = h >= 4 ? (h != 12 && h != 14 ? z : x) : 0.0D;
        return ((h & 1) != 0 ? -u : u) + ((h & 2) != 0 ? -v : v);
    }

    double sample(double x, double y, double z) {
        double fx = x + xo;
        double fy = y + yo;
        double fz = z + zo;
        int ix = (int) fx;
        int iy = (int) fy;
        int iz = (int) fz;
        if (fx < (double) ix) ix--;
        if (fy < (double) iy) iy--;
        if (fz < (double) iz) iz--;
        int X = ix & 255;
        int Y = iy & 255;
        int Z = iz & 255;
        fx -= ix;
        fy -= iy;
        fz -= iz;
        double u = fx * fx * fx * (fx * (fx * 6.0D - 15.0D) + 10.0D);
        double v = fy * fy * fy * (fy * (fy * 6.0D - 15.0D) + 10.0D);
        double w = fz * fz * fz * (fz * (fz * 6.0D - 15.0D) + 10.0D);
        int a = p[X] + Y;
        int aa = p[a] + Z;
        int ab = p[a + 1] + Z;
        int b = p[X + 1] + Y;
        int ba = p[b] + Z;
        int bb = p[b + 1] + Z;
        return lerp(w,
                lerp(v,
                        lerp(u, grad(p[aa], fx, fy, fz), grad(p[ba], fx - 1.0D, fy, fz)),
                        lerp(u, grad(p[ab], fx, fy - 1.0D, fz), grad(p[bb], fx - 1.0D, fy - 1.0D, fz))),
                lerp(v,
                        lerp(u, grad(p[aa + 1], fx, fy, fz - 1.0D), grad(p[ba + 1], fx - 1.0D, fy, fz - 1.0D)),
                        lerp(u, grad(p[ab + 1], fx, fy - 1.0D, fz - 1.0D), grad(p[bb + 1], fx - 1.0D, fy - 1.0D, fz - 1.0D))));
    }

    void add(double[] out, double x, double y, double z, int sizeX, int sizeY, int sizeZ,
             double scaleX, double scaleY, double scaleZ, double amplitude) {
        if (sizeY == 1) {
            int index = 0;
            double inverse = 1.0D / amplitude;
            for (int ix = 0; ix < sizeX; ix++) {
                double fx = (x + (double) ix) * scaleX + xo;
                int cx = (int) fx;
                if (fx < (double) cx) cx--;
                int X = cx & 255;
                fx -= cx;
                double u = fx * fx * fx * (fx * (fx * 6.0D - 15.0D) + 10.0D);
                for (int iz = 0; iz < sizeZ; iz++) {
                    double fz = (z + (double) iz) * scaleZ + zo;
                    int cz = (int) fz;
                    if (fz < (double) cz) cz--;
                    int Z = cz & 255;
                    fz -= cz;
                    double w = fz * fz * fz * (fz * (fz * 6.0D - 15.0D) + 10.0D);
                    int a = p[X];
                    int aa = p[a] + Z;
                    int b = p[X + 1];
                    int ba = p[b] + Z;
                    double near = lerp(u, grad2(p[aa], fx, fz), grad(p[ba], fx - 1.0D, 0.0D, fz));
                    double far = lerp(u, grad(p[aa + 1], fx, 0.0D, fz - 1.0D), grad(p[ba + 1], fx - 1.0D, 0.0D, fz - 1.0D));
                    out[index++] += lerp(w, near, far) * inverse;
                }
            }
            return;
        }

        int index = 0;
        double inverse = 1.0D / amplitude;
        int lastY = -1;
        double x0y0 = 0.0D;
        double x0y1 = 0.0D;
        double x1y0 = 0.0D;
        double x1y1 = 0.0D;
        for (int ix = 0; ix < sizeX; ix++) {
            double fx = (x + (double) ix) * scaleX + xo;
            int cx = (int) fx;
            if (fx < (double) cx) cx--;
            int X = cx & 255;
            fx -= cx;
            double u = fx * fx * fx * (fx * (fx * 6.0D - 15.0D) + 10.0D);
            for (int iz = 0; iz < sizeZ; iz++) {
                double fz = (z + (double) iz) * scaleZ + zo;
                int cz = (int) fz;
                if (fz < (double) cz) cz--;
                int Z = cz & 255;
                fz -= cz;
                double w = fz * fz * fz * (fz * (fz * 6.0D - 15.0D) + 10.0D);
                for (int iy = 0; iy < sizeY; iy++) {
                    double fy = (y + (double) iy) * scaleY + yo;
                    int cy = (int) fy;
                    if (fy < (double) cy) cy--;
                    int Y = cy & 255;
                    fy -= cy;
                    double v = fy * fy * fy * (fy * (fy * 6.0D - 15.0D) + 10.0D);
                    if (iy == 0 || Y != lastY) {
                        lastY = Y;
                        int a = p[X] + Y;
                        int aa = p[a] + Z;
                        int ab = p[a + 1] + Z;
                        int b = p[X + 1] + Y;
                        int ba = p[b] + Z;
                        int bb = p[b + 1] + Z;
                        x0y0 = lerp(u, grad(p[aa], fx, fy, fz), grad(p[ba], fx - 1.0D, fy, fz));
                        x0y1 = lerp(u, grad(p[ab], fx, fy - 1.0D, fz), grad(p[bb], fx - 1.0D, fy - 1.0D, fz));
                        x1y0 = lerp(u, grad(p[aa + 1], fx, fy, fz - 1.0D), grad(p[ba + 1], fx - 1.0D, fy, fz - 1.0D));
                        x1y1 = lerp(u, grad(p[ab + 1], fx, fy - 1.0D, fz - 1.0D), grad(p[bb + 1], fx - 1.0D, fy - 1.0D, fz - 1.0D));
                    }
                    double near = lerp(v, x0y0, x0y1);
                    double far = lerp(v, x1y0, x1y1);
                    out[index++] += lerp(w, near, far) * inverse;
                }
            }
        }
    }
}
