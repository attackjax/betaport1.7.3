package dev.b173.client.mixin.gui;

import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DeathScreen.class)
public interface DeathScreenInvoker {
    @Invoker("exitToTitleScreen")
    void b173$exitToTitle();
}
