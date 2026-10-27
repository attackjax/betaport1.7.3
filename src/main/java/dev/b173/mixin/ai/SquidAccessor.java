package dev.b173.mixin.ai;

import net.minecraft.world.entity.animal.squid.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Squid.class)
public interface SquidAccessor {
    @Accessor("tentacleSpeed")
    float b173$tentacleSpeed();

    @Accessor("tentacleSpeed")
    void b173$setTentacleSpeed(float speed);

    @Accessor("rotateSpeed")
    float b173$rotateSpeed();

    @Accessor("rotateSpeed")
    void b173$setRotateSpeed(float speed);
}
