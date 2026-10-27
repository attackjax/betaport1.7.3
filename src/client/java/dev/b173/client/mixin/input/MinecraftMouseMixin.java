package dev.b173.client.mixin.input;

import dev.b173.rules.BetaRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMouseMixin {
    @Unique
    private static final int B173_REPEAT_TICKS = 5;

    @Shadow
    public HitResult hitResult;

    @Shadow
    public LocalPlayer player;

    @Unique
    private int b173$keybindTicks;

    @Unique
    private int b173$lastAttackTick;

    @Shadow
    private boolean startAttack() {
        throw new AssertionError();
    }

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void b173$markAttack(CallbackInfoReturnable<Boolean> callback) {
        b173$lastAttackTick = b173$keybindTicks;
    }

    @Inject(method = "continueAttack", at = @At("HEAD"))
    private void b173$holdToAttack(boolean leftClick, CallbackInfo callback) {
        if (leftClick && BetaRules.clientInBetaWorld
                && b173$keybindTicks - b173$lastAttackTick >= B173_REPEAT_TICKS
                && hitResult != null && hitResult.getType() != HitResult.Type.BLOCK
                && player != null && !player.isUsingItem()) {
            startAttack();
        }
        b173$keybindTicks++;
    }

    @ModifyConstant(method = "startUseItem", constant = @Constant(intValue = 4))
    private int b173$betaUseRepeat(int delay) {
        return BetaRules.clientInBetaWorld ? B173_REPEAT_TICKS : delay;
    }
}
