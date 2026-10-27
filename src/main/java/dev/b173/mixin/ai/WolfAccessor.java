package dev.b173.mixin.ai;

import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Wolf.class)
public interface WolfAccessor {
    @Accessor("isShaking")
    boolean b173$isShaking();
}
