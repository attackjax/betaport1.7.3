package dev.b173.client.visual;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

public final class BetaIris {
    private static final Object API;
    private static final Method IN_USE;

    static {
        Object api = null;
        Method inUse = null;
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            try {
                Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = type.getMethod("getInstance").invoke(null);
                inUse = type.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                api = null;
                inUse = null;
            }
        }
        API = api;
        IN_USE = inUse;
    }

    private BetaIris() {
    }

    public static boolean shadersInUse() {
        if (IN_USE == null) {
            return false;
        }
        try {
            return (Boolean) IN_USE.invoke(API);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
