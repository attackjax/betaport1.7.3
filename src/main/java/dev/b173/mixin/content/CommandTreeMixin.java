package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(Commands.class)
abstract class CommandTreeMixin {
    @WrapOperation(method = "sendCommands", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/commands/Commands;fillUsableCommands(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/tree/CommandNode;Ljava/lang/Object;Ljava/util/Map;)V"))
    private void b173$noneInSinglePlayer(CommandNode<?> root, CommandNode<?> target, Object source, Map<?, ?> map,
                                         Operation<Void> original, @Local(argsOnly = true) ServerPlayer player) {
        if (player.level().getServer().isDedicatedServer()) {
            original.call(root, target, source, map);
        }
    }
}
