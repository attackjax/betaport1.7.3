package dev.b173.client.mixin.sky;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {
    @Accessor("skyFlashTime")
    int b173$skyFlashTime();
}
