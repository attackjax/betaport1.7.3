package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
abstract class BetaClicksMixin extends Screen {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    @Final
    protected int imageWidth;

    @Shadow
    @Final
    protected int imageHeight;

    private BetaClicksMixin(Component title) {
        super(title);
    }

    @Shadow
    private Slot getHoveredSlot(double x, double y) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract void slotClicked(Slot slot, int slotId, int button, ContainerInput input);

    @Unique
    private boolean b173$beta() {
        return BetaScreens.active() && dev.b173.rules.BetaClicks.enforced()
                && !((Object) this instanceof CreativeModeInventoryScreen);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void b173$betaClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!b173$beta() || getChildAt(event.x(), event.y()).isPresent()) {
            return;
        }
        cir.setReturnValue(true);
        int button = event.button();
        if (button != 0 && button != 1) {
            return;
        }
        Slot slot = getHoveredSlot(event.x(), event.y());
        boolean outside = event.x() < this.leftPos || event.y() < this.topPos
                || event.x() >= this.leftPos + this.imageWidth || event.y() >= this.topPos + this.imageHeight;
        if (outside) {
            slotClicked(null, -999, button, ContainerInput.PICKUP);
        } else if (slot != null) {
            slotClicked(slot, slot.index, button, event.hasShiftDown() ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void b173$noDrag(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (b173$beta() && getChildAt(event.x(), event.y()).isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void b173$noRelease(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (b173$beta() && getChildAt(event.x(), event.y()).isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "checkHotbarKeyPressed", at = @At("HEAD"), cancellable = true)
    private void b173$noHotbarKeys(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (b173$beta()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void b173$onlyBetaClicks(Slot slot, int slotId, int button, ContainerInput input, CallbackInfo ci) {
        if (b173$beta() && input != ContainerInput.PICKUP && input != ContainerInput.QUICK_MOVE) {
            ci.cancel();
        }
    }
}
