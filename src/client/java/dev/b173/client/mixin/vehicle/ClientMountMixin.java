package dev.b173.client.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.b173.rules.BetaRules;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
abstract class ClientMountMixin {
    @WrapWithCondition(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private boolean b173$noHint(Gui gui, Component message, boolean animate) {
        return !BetaRules.clientInBetaWorld;
    }

    @WrapWithCondition(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/GameNarrator;saySystemNow(Lnet/minecraft/network/chat/Component;)V"))
    private boolean b173$noSpokenHint(GameNarrator narrator, Component message) {
        return !BetaRules.clientInBetaWorld;
    }

    @ModifyExpressionValue(method = "handleSetEntityPassengersPacket", at = @At(value = "CONSTANT",
            args = "classValue=net/minecraft/world/entity/vehicle/boat/AbstractBoat"))
    private boolean b173$keepFacing(boolean boat) {
        return boat && !BetaRules.clientInBetaWorld;
    }
}
