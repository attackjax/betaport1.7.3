package dev.b173.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.client.visual.BetaJarLocator;
import dev.b173.core.pack.BetaSounds;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
abstract class SoundEngineMixin {
    @Unique
    private static final Logger B173_LOGGER = LoggerFactory.getLogger("Betaport");
    @Unique
    private static final boolean B173_LOG_SOUNDS = "true".equalsIgnoreCase(BetaJarLocator.configured(
            FabricLoader.getInstance().getConfigDir().resolve("b173.properties"), "logSounds"));

    @ModifyExpressionValue(method = "play", at = @At(value = "INVOKE",
            target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean b173$quietSilenced(boolean firstTime, @Local(argsOnly = true) SoundInstance sound) {
        Identifier id = sound.getIdentifier();
        return firstTime && !(id.getNamespace().equals("minecraft") && BetaSounds.silenced(id.getPath()));
    }

    @Inject(method = "play", at = @At("RETURN"))
    private void b173$logSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (!B173_LOG_SOUNDS) {
            return;
        }
        Sound picked = sound.getSound();
        String volume;
        try {
            volume = String.format("volume %.2f pitch %.2f", sound.getVolume(), sound.getPitch());
        } catch (RuntimeException e) {
            volume = "volume ?";
        }
        B173_LOGGER.info("b173 sound {} -> {}: {}, {}, {} at {} {} {}", sound.getIdentifier(),
                picked == null ? "nothing" : picked.getLocation(), cir.getReturnValue(), volume, sound.getSource(),
                String.format("%.1f", sound.getX()), String.format("%.1f", sound.getY()), String.format("%.1f", sound.getZ()));
    }
}
