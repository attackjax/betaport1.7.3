package dev.b173.rules;

import dev.b173.core.BetaMath;
import dev.b173.mixin.vehicle.EntityBlockEffectsInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public final class BetaBoat {
    public static final double Y_OFFSET = 0.3D;

    private static final Map<EntityType<?>, Item> PLANKS = Map.of(
            EntityType.OAK_BOAT, Items.OAK_PLANKS,
            EntityType.SPRUCE_BOAT, Items.SPRUCE_PLANKS,
            EntityType.BIRCH_BOAT, Items.BIRCH_PLANKS,
            EntityType.JUNGLE_BOAT, Items.JUNGLE_PLANKS,
            EntityType.ACACIA_BOAT, Items.ACACIA_PLANKS,
            EntityType.CHERRY_BOAT, Items.CHERRY_PLANKS,
            EntityType.DARK_OAK_BOAT, Items.DARK_OAK_PLANKS,
            EntityType.PALE_OAK_BOAT, Items.PALE_OAK_PLANKS,
            EntityType.MANGROVE_BOAT, Items.MANGROVE_PLANKS);

    private BetaBoat() {
    }

    public static boolean isBeta(Entity entity) {
        return entity instanceof Boat && BetaRules.betaWorld(entity.level());
    }

    public static boolean isBoatType(EntityType<?> type) {
        return PLANKS.containsKey(type);
    }

    public static void tick(AbstractBoat boat) {
        if (boat.getPaddleState(0) || boat.getPaddleState(1)) {
            boat.setPaddleState(false, false);
        }
        if (!(boat.level() instanceof ServerLevel level) || !boat.isLocalInstanceAuthoritative()) {
            boat.setDeltaMovement(Vec3.ZERO);
            ((EntityBlockEffectsInvoker) boat).b173$applyEffectsFromBlocks();
            return;
        }
        AABB box = boat.getBoundingBox();
        int slices = 5;
        double water = 0.0D;
        for (int i = 0; i < slices; i++) {
            double bottom = box.minY + (box.maxY - box.minY) * (double) (i + 0) / (double) slices - 0.125D;
            double top = box.minY + (box.maxY - box.minY) * (double) (i + 1) / (double) slices - 0.125D;
            if (inWater(level, new AABB(box.minX, bottom, box.minZ, box.maxX, top, box.maxZ))) {
                water += 1.0D / (double) slices;
            }
        }
        Vec3 motion = boat.getDeltaMovement().add(waterPush(level, box));
        double mx = motion.x;
        double my = motion.y;
        double mz = motion.z;
        if (water < 1.0D) {
            double rise = water * 2.0D - 1.0D;
            my += (double) 0.04F * rise;
        } else {
            if (my < 0.0D) {
                my /= 2.0D;
            }
            my += (double) 0.007F;
        }
        Entity rider = boat.getFirstPassenger();
        if (rider != null) {
            Vec3 own = ((BetaBoatRider) boat).b173$riderMotion();
            mx += own.x * 0.2D;
            mz += own.z * 0.2D;
        }
        double maxSpeed = 0.4D;
        mx = Mth.clamp(mx, -maxSpeed, maxSpeed);
        mz = Mth.clamp(mz, -maxSpeed, maxSpeed);
        if (boat.onGround()) {
            mx *= 0.5D;
            my *= 0.5D;
            mz *= 0.5D;
        }
        boat.setDeltaMovement(mx, my, mz);
        boat.move(MoverType.SELF, new Vec3(mx, my, mz));
        ((EntityBlockEffectsInvoker) boat).b173$applyEffectsFromBlocks();
        motion = boat.getDeltaMovement();
        mx = motion.x;
        my = motion.y;
        mz = motion.z;
        double speed = Math.sqrt(mx * mx + mz * mz);
        if (speed > 0.15D) {
            spray(level, boat, speed, mx, my, mz);
        }
        if (boat.horizontalCollision && speed > 0.15D) {
            wreck(level, boat);
        } else {
            mx *= (double) 0.99F;
            my *= (double) 0.95F;
            mz *= (double) 0.99F;
        }
        boat.setDeltaMovement(mx, my, mz);
        face(boat);
        AABB reach = boat.getBoundingBox().inflate((double) 0.2F, 0.0D, (double) 0.2F);
        for (Entity other : level.getEntities(boat, reach)) {
            if (other != rider && other.isPushable() && other instanceof Boat) {
                BetaVehicles.push(other, boat);
            }
        }
        double posY = boat.getY() + Y_OFFSET;
        for (int i = 0; i < 4; i++) {
            int x = Mth.floor(boat.getX() + ((double) (i % 2) - 0.5D) * 0.8D);
            int y = Mth.floor(posY);
            int z = Mth.floor(boat.getZ() + ((double) (i / 2) - 0.5D) * 0.8D);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(Blocks.SNOW)) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
        ((BetaBoatRider) boat).b173$setRiderMotion(rider != null && rider.getVehicle() == boat
                ? riderMotion(level, rider) : Vec3.ZERO);
        if (rider instanceof ServerPlayer) {
            boat.needsSync = true;
        }
    }

    private static void face(AbstractBoat boat) {
        float yaw = boat.getYRot() - 90.0F;
        double target = (double) yaw;
        double backX = boat.xo - boat.getX();
        double backZ = boat.zo - boat.getZ();
        if (backX * backX + backZ * backZ > 0.001D) {
            target = (double) (float) (Math.atan2(backZ, backX) * 180.0D / Math.PI);
        }
        double turn = target - (double) yaw;
        while (turn >= 180.0D) {
            turn -= 360.0D;
        }
        while (turn < -180.0D) {
            turn += 360.0D;
        }
        if (turn > 20.0D) {
            turn = 20.0D;
        }
        if (turn < -20.0D) {
            turn = -20.0D;
        }
        yaw = (float) ((double) yaw + turn);
        boat.setYRot(yaw % 360.0F + 90.0F);
        boat.setXRot(0.0F);
    }

    public static boolean inWater(Level level, AABB box) {
        int x0 = Mth.floor(box.minX);
        int x1 = Mth.floor(box.maxX + 1.0D);
        int y0 = Mth.floor(box.minY);
        int y1 = Mth.floor(box.maxY + 1.0D);
        int z0 = Mth.floor(box.minZ);
        int z1 = Mth.floor(box.maxZ + 1.0D);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (!state.is(Blocks.WATER)) {
                        continue;
                    }
                    int meta = state.getValue(LiquidBlock.LEVEL);
                    double surface = (double) (y + 1);
                    if (meta < 8) {
                        surface = (double) (y + 1) - (double) meta / 8.0D;
                    }
                    if (surface >= box.minY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Vec3 waterPush(Level level, AABB box) {
        AABB in = shrink(box);
        int x0 = Mth.floor(in.minX);
        int x1 = Mth.floor(in.maxX + 1.0D);
        int y0 = Mth.floor(in.minY);
        int y1 = Mth.floor(in.maxY + 1.0D);
        int z0 = Mth.floor(in.minZ);
        int z1 = Mth.floor(in.maxZ + 1.0D);
        if (!level.hasChunksAt(x0, z0, x1, z1)) {
            return Vec3.ZERO;
        }
        Vec3 flow = Vec3.ZERO;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    pos.set(x, y, z);
                    FluidState fluid = level.getFluidState(pos);
                    if (fluid.is(FluidTags.WATER) && level.getBlockState(pos).is(Blocks.WATER)) {
                        flow = flow.add(fluid.getFlow(level, pos));
                    }
                }
            }
        }
        if (flow.length() > 0.0D) {
            return flow.normalize().scale(0.014D);
        }
        return Vec3.ZERO;
    }

    private static boolean touchesWater(Level level, AABB box) {
        AABB in = shrink(box);
        int x0 = Mth.floor(in.minX);
        int x1 = Mth.floor(in.maxX + 1.0D);
        int y0 = Mth.floor(in.minY);
        int y1 = Mth.floor(in.maxY + 1.0D);
        int z0 = Mth.floor(in.minZ);
        int z1 = Mth.floor(in.maxZ + 1.0D);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (level.getBlockState(pos.set(x, y, z)).is(Blocks.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static AABB shrink(AABB box) {
        return new AABB(box.minX + 0.001D, box.minY + 0.4D + 0.001D, box.minZ + 0.001D,
                box.maxX - 0.001D, box.maxY - 0.4D - 0.001D, box.maxZ - 0.001D);
    }

    public static Vec3 riderMotion(Level level, Entity rider) {
        if (!(rider instanceof ServerPlayer player)) {
            return Vec3.ZERO;
        }
        Input input = player.getLastClientInput();
        float forward = 0.0F;
        float strafe = 0.0F;
        if (input.forward()) {
            forward++;
        }
        if (input.backward()) {
            forward--;
        }
        if (input.left()) {
            strafe++;
        }
        if (input.right()) {
            strafe--;
        }
        if (input.shift()) {
            strafe = (float) ((double) strafe * 0.3D);
            forward = (float) ((double) forward * 0.3D);
        }
        strafe *= 0.98F;
        forward *= 0.98F;
        AABB box = player.getBoundingBox();
        boolean water = touchesWater(level, box);
        Vec3 motion = water ? waterPush(level, box) : Vec3.ZERO;
        double mx = motion.x;
        double mz = motion.z;
        float keys = Mth.sqrt(strafe * strafe + forward * forward);
        if (keys >= 0.01F) {
            if (keys < 1.0F) {
                keys = 1.0F;
            }
            keys = 0.02F / keys;
            strafe *= keys;
            forward *= keys;
            float sin = BetaMath.sin(player.getYRot() * 3.1415927F / 180.0F);
            float cos = BetaMath.cos(player.getYRot() * 3.1415927F / 180.0F);
            mx += (double) (strafe * cos - forward * sin);
            mz += (double) (forward * cos + strafe * sin);
        }
        double kept = water ? (double) 0.8F : (double) 0.91F;
        return new Vec3(mx * kept, 0.0D, mz * kept);
    }

    private static void spray(ServerLevel level, AbstractBoat boat, double speed, double mx, double my, double mz) {
        double betaYaw = (double) (boat.getYRot() - 90.0F);
        double cos = Math.cos(betaYaw * Math.PI / 180.0D);
        double sin = Math.sin(betaYaw * Math.PI / 180.0D);
        double posY = boat.getY() + Y_OFFSET;
        for (int i = 0; (double) i < 1.0D + speed * 60.0D; i++) {
            double across = (double) (boat.getRandom().nextFloat() * 2.0F - 1.0F);
            double side = (double) (boat.getRandom().nextInt(2) * 2 - 1) * 0.7D;
            double x;
            double z;
            if (boat.getRandom().nextBoolean()) {
                x = boat.getX() - cos * across * 0.8D + sin * side;
                z = boat.getZ() - sin * across * 0.8D - cos * side;
            } else {
                x = boat.getX() + cos + sin * across * 0.7D;
                z = boat.getZ() + sin - cos * across * 0.7D;
            }
            level.sendParticles(ParticleTypes.SPLASH, x, posY - 0.125D, z, 0, mx, my, mz, 1.0D);
        }
    }

    public static void wreck(ServerLevel level, AbstractBoat boat) {
        boat.kill(level);
        dropWreckage(level, boat);
    }

    public static void dropWreckage(ServerLevel level, AbstractBoat boat) {
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }
        Item planks = PLANKS.getOrDefault(boat.getType(), Items.OAK_PLANKS);
        for (int i = 0; i < 3; i++) {
            boat.spawnAtLocation(level, new ItemStack(planks), (float) Y_OFFSET);
        }
        for (int i = 0; i < 2; i++) {
            boat.spawnAtLocation(level, new ItemStack(Items.STICK), (float) Y_OFFSET);
        }
    }
}
