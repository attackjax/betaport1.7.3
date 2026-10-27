package dev.b173.core;

import java.util.Random;

public final class BetaSky {
    private static final float FOG_RED = 0.7529412F;
    private static final float FOG_GREEN = 0.84705883F;
    private static final float FOG_BLUE = 1.0F;

    private BetaSky() {
    }

    public static int skyDarkening(long time, float partialTick, float rain, float thunder) {
        float angle = celestialAngle(time, partialTick);
        float f = 1.0F - (BetaMath.cos(angle * 3.1415927F * 2.0F) * 2.0F + 0.5F);
        if (f < 0.0F) {
            f = 0.0F;
        }
        if (f > 1.0F) {
            f = 1.0F;
        }
        f = 1.0F - f;
        f = (float) ((double) f * (1.0D - (double) (rain * 5.0F) / 16.0D));
        f = (float) ((double) f * (1.0D - (double) (thunder * 5.0F) / 16.0D));
        f = 1.0F - f;
        return (int) (f * 11.0F);
    }

    public static float celestialAngle(long time, float partialTick) {
        int ticks = (int) (time % 24000L);
        float f = ((float) ticks + partialTick) / 24000.0F - 0.25F;
        if (f < 0.0F) {
            f++;
        }
        if (f > 1.0F) {
            f--;
        }
        float linear = f;
        f = 1.0F - (float) ((Math.cos((double) f * Math.PI) + 1.0D) / 2.0D);
        return linear + (f - linear) / 3.0F;
    }

    public static float daylight(float angle) {
        float f = BetaMath.cos(angle * 3.1415927F * 2.0F) * 2.0F + 0.5F;
        if (f < 0.0F) {
            f = 0.0F;
        }
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    public static int skyColorByTemperature(float temperature) {
        float f = temperature / 3.0F;
        if (f < -1.0F) {
            f = -1.0F;
        }
        if (f > 1.0F) {
            f = 1.0F;
        }
        return hsbToRgb(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1.0F);
    }

    public static void skyColor(int rgb, float angle, float rain, float thunder, int flashTicks, float partialTick,
                                float[] out) {
        float day = daylight(angle);
        float r = (float) (rgb >> 16 & 255) / 255.0F;
        float g = (float) (rgb >> 8 & 255) / 255.0F;
        float b = (float) (rgb & 255) / 255.0F;
        r *= day;
        g *= day;
        b *= day;
        if (rain > 0.0F) {
            float grey = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
            float keep = 1.0F - rain * 0.75F;
            r = r * keep + grey * (1.0F - keep);
            g = g * keep + grey * (1.0F - keep);
            b = b * keep + grey * (1.0F - keep);
        }
        if (thunder > 0.0F) {
            float grey = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
            float keep = 1.0F - thunder * 0.75F;
            r = r * keep + grey * (1.0F - keep);
            g = g * keep + grey * (1.0F - keep);
            b = b * keep + grey * (1.0F - keep);
        }
        if (flashTicks > 0) {
            float flash = (float) flashTicks - partialTick;
            if (flash > 1.0F) {
                flash = 1.0F;
            }
            flash *= 0.45F;
            r = r * (1.0F - flash) + 0.8F * flash;
            g = g * (1.0F - flash) + 0.8F * flash;
            b = b * (1.0F - flash) + 1.0F * flash;
        }
        out[0] = r;
        out[1] = g;
        out[2] = b;
    }

    public static void cloudColor(float angle, float rain, float thunder, float[] out) {
        float day = daylight(angle);
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        if (rain > 0.0F) {
            float grey = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
            float keep = 1.0F - rain * 0.95F;
            r = r * keep + grey * (1.0F - keep);
            g = g * keep + grey * (1.0F - keep);
            b = b * keep + grey * (1.0F - keep);
        }
        r *= day * 0.9F + 0.1F;
        g *= day * 0.9F + 0.1F;
        b *= day * 0.85F + 0.15F;
        if (thunder > 0.0F) {
            float grey = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
            float keep = 1.0F - thunder * 0.95F;
            r = r * keep + grey * (1.0F - keep);
            g = g * keep + grey * (1.0F - keep);
            b = b * keep + grey * (1.0F - keep);
        }
        out[0] = r;
        out[1] = g;
        out[2] = b;
    }

    public static void fogColor(float angle, float[] out) {
        float day = daylight(angle);
        out[0] = FOG_RED * (day * 0.94F + 0.06F);
        out[1] = FOG_GREEN * (day * 0.94F + 0.06F);
        out[2] = FOG_BLUE * (day * 0.91F + 0.09F);
    }

    public static boolean sunriseColor(float angle, float[] out) {
        float width = 0.4F;
        float f = BetaMath.cos(angle * 3.1415927F * 2.0F) - 0.0F;
        float centre = -0.0F;
        if (f >= centre - width && f <= centre + width) {
            float t = (f - centre) / width * 0.5F + 0.5F;
            float alpha = 1.0F - (1.0F - BetaMath.sin(t * 3.1415927F)) * 0.99F;
            alpha *= alpha;
            out[0] = t * 0.3F + 0.7F;
            out[1] = t * t * 0.7F + 0.2F;
            out[2] = t * t * 0.0F + 0.2F;
            out[3] = alpha;
            return true;
        }
        return false;
    }

    public static float starBrightness(float angle) {
        float f = 1.0F - (BetaMath.cos(angle * 3.1415927F * 2.0F) * 2.0F + 0.75F);
        if (f < 0.0F) {
            f = 0.0F;
        }
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f * f * 0.5F;
    }

    public static float[] stars() {
        Random random = new Random(10842L);
        float[] corners = new float[1500 * 4 * 3];
        int n = 0;
        for (int i = 0; i < 1500; i++) {
            double x = (double) (random.nextFloat() * 2.0F - 1.0F);
            double y = (double) (random.nextFloat() * 2.0F - 1.0F);
            double z = (double) (random.nextFloat() * 2.0F - 1.0F);
            double size = (double) (0.25F + random.nextFloat() * 0.25F);
            double lengthSq = x * x + y * y + z * z;
            if (lengthSq < 1.0D && lengthSq > 0.01D) {
                double scale = 1.0D / Math.sqrt(lengthSq);
                x *= scale;
                y *= scale;
                z *= scale;
                double cx = x * 100.0D;
                double cy = y * 100.0D;
                double cz = z * 100.0D;
                double yaw = Math.atan2(x, z);
                double sinYaw = Math.sin(yaw);
                double cosYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sinPitch = Math.sin(pitch);
                double cosPitch = Math.cos(pitch);
                double spin = random.nextDouble() * Math.PI * 2.0D;
                double sinSpin = Math.sin(spin);
                double cosSpin = Math.cos(spin);
                for (int corner = 0; corner < 4; corner++) {
                    double depth = 0.0D;
                    double across = (double) ((corner & 2) - 1) * size;
                    double up = (double) ((corner + 1 & 2) - 1) * size;
                    double spunAcross = across * cosSpin - up * sinSpin;
                    double spunUp = up * cosSpin + across * sinSpin;
                    double pitchedY = spunAcross * sinPitch + depth * cosPitch;
                    double pitchedDepth = depth * sinPitch - spunAcross * cosPitch;
                    double dx = pitchedDepth * sinYaw - spunUp * cosYaw;
                    double dz = spunUp * sinYaw + pitchedDepth * cosYaw;
                    corners[n++] = (float) (cx + dx);
                    corners[n++] = (float) (cy + pitchedY);
                    corners[n++] = (float) (cz + dz);
                }
            }
        }
        return java.util.Arrays.copyOf(corners, n);
    }

    public static float[] skyPlane(boolean above) {
        int step = 64;
        int reach = step * (256 / step + 2);
        float y = above ? 16.0F : -16.0F;
        int squares = (2 * reach / step + 1) * (2 * reach / step + 1);
        float[] corners = new float[squares * 4 * 3];
        int n = 0;
        for (int a = -reach; a <= reach; a += step) {
            for (int b = -reach; b <= reach; b += step) {
                if (above) {
                    n = corner(corners, n, a, y, b);
                    n = corner(corners, n, a + step, y, b);
                    n = corner(corners, n, a + step, y, b + step);
                    n = corner(corners, n, a, y, b + step);
                } else {
                    n = corner(corners, n, a + step, y, b);
                    n = corner(corners, n, a, y, b);
                    n = corner(corners, n, a, y, b + step);
                    n = corner(corners, n, a + step, y, b + step);
                }
            }
        }
        return corners;
    }

    private static int corner(float[] corners, int n, int x, float y, int z) {
        corners[n] = (float) x;
        corners[n + 1] = y;
        corners[n + 2] = (float) z;
        return n + 3;
    }

    static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0.0F) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0F;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - saturation * (1.0F - f));
            switch ((int) h) {
                case 0 -> {
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (t * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                }
                case 1 -> {
                    r = (int) (q * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                }
                case 2 -> {
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (t * 255.0F + 0.5F);
                }
                case 3 -> {
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (q * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                }
                case 4 -> {
                    r = (int) (t * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                }
                case 5 -> {
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (q * 255.0F + 0.5F);
                }
                default -> {
                }
            }
        }
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
