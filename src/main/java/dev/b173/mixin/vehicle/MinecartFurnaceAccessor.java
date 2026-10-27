package dev.b173.mixin.vehicle;

import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecartFurnace.class)
public interface MinecartFurnaceAccessor {
    @Accessor("fuel")
    int b173$getFuel();

    @Accessor("fuel")
    void b173$setFuel(int fuel);

    @Invoker("setHasFuel")
    void b173$setHasFuel(boolean hasFuel);

    @Invoker("hasFuel")
    boolean b173$hasFuel();
}
