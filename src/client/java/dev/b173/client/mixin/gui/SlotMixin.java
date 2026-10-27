package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
abstract class SlotMixin implements BetaSlot {
    @Shadow
    @Final
    @Mutable
    public int x;

    @Shadow
    @Final
    @Mutable
    public int y;

    @Unique
    private boolean b173$hidden;

    @Override
    public void b173$moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void b173$setHidden(boolean hidden) {
        this.b173$hidden = hidden;
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void b173$hidden(CallbackInfoReturnable<Boolean> cir) {
        if (this.b173$hidden) {
            cir.setReturnValue(false);
        }
    }
}
