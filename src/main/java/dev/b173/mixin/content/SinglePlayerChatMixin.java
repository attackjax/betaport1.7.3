package dev.b173.mixin.content;

import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class SinglePlayerChatMixin {
    @Shadow
    public ServerPlayer player;

    private boolean b173$singlePlayer() {
        return !this.player.level().getServer().isDedicatedServer();
    }

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void b173$noChat(ServerboundChatPacket packet, CallbackInfo ci) {
        if (b173$singlePlayer()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleChatCommand", at = @At("HEAD"), cancellable = true)
    private void b173$noCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        if (b173$singlePlayer()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSignedChatCommand", at = @At("HEAD"), cancellable = true)
    private void b173$noSignedCommand(ServerboundChatCommandSignedPacket packet, CallbackInfo ci) {
        if (b173$singlePlayer()) {
            ci.cancel();
        }
    }
}
