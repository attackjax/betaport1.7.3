package dev.b173.client.visual;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class BetaVoxy {
    private static final Logger LOGGER = LoggerFactory.getLogger("Betaport");

    private static final float BLOCKS_PER_SECTION = 256.0F;

    private static final MethodHandle CONFIG;
    private static final MethodHandle RENDERING_ENABLED;
    private static final MethodHandle SECTION_DISTANCE;
    private static final MethodHandle RENDER_SYSTEM;

    static {
        MethodHandle config = null;
        MethodHandle rendering = null;
        MethodHandle distance = null;
        MethodHandle system = null;
        if (FabricLoader.getInstance().isModLoaded("voxy")) {
            try {
                ClassLoader loader = BetaVoxy.class.getClassLoader();
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                Class<?> configClass = Class.forName("me.cortex.voxy.client.config.VoxyConfig", false, loader);
                Class<?> holderClass = Class.forName("me.cortex.voxy.client.core.IVoxyRenderSystemHolder", false, loader);
                Class<?> systemClass = Class.forName("me.cortex.voxy.client.core.VoxyRenderSystem", false, loader);
                config = lookup.findStaticGetter(configClass, "CONFIG", configClass);
                rendering = lookup.findVirtual(configClass, "isRenderingEnabled", MethodType.methodType(boolean.class));
                distance = lookup.findGetter(configClass, "sectionRenderDistance", float.class);
                system = lookup.findStatic(holderClass, "getNullable", MethodType.methodType(systemClass));
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.warn("b173: this Voxy is not the one the Beta fog knows how to read ({}); "
                        + "the fog will end at the normal render distance", e.toString());
                config = null;
            }
        }
        CONFIG = config;
        RENDERING_ENABLED = rendering;
        SECTION_DISTANCE = distance;
        RENDER_SYSTEM = system;
    }

    private static boolean failed;

    private BetaVoxy() {
    }

    public static float lodDistance() {
        if (CONFIG == null || failed) {
            return 0.0F;
        }
        try {
            Object config = CONFIG.invoke();
            if (config == null || !(boolean) RENDERING_ENABLED.invoke(config) || RENDER_SYSTEM.invoke() == null) {
                return 0.0F;
            }
            return Math.max((float) SECTION_DISTANCE.invoke(config), 0.0F) * BLOCKS_PER_SECTION;
        } catch (Throwable e) {
            failed = true;
            LOGGER.warn("b173: could not read Voxy's render distance ({}); "
                    + "the fog will end at the normal render distance", e.toString());
            return 0.0F;
        }
    }
}
