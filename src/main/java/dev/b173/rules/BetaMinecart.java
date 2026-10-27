package dev.b173.rules;

import dev.b173.mixin.vehicle.MinecartFurnaceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BetaMinecart {
    public static final double Y_OFFSET = 0.35D;

    private static final int[][][] EXITS = {
            {{0, 0, -1}, {0, 0, 1}},
            {{-1, 0, 0}, {1, 0, 0}},
            {{-1, -1, 0}, {1, 0, 0}},
            {{-1, 0, 0}, {1, -1, 0}},
            {{0, 0, -1}, {0, -1, 1}},
            {{0, -1, -1}, {0, 0, 1}},
            {{0, 0, 1}, {1, 0, 0}},
            {{0, 0, 1}, {-1, 0, 0}},
            {{0, 0, -1}, {-1, 0, 0}},
            {{0, 0, -1}, {1, 0, 0}}};

    public static final int COAL_FUEL = 1200;

    private BetaMinecart() {
    }

    public static double sqrtDouble(double value) {
        return (double) (float) Math.sqrt(value);
    }

    private static boolean isRail(Level level, int x, int y, int z) {
        return BaseRailBlock.isRail(level.getBlockState(new BlockPos(x, y, z)));
    }

    private static int shape(BlockState state) {
        return state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty()).ordinal();
    }

    public static void tick(AbstractMinecart cart, ServerLevel level) {
        Vec3 motion = cart.getDeltaMovement();
        double mx = motion.x;
        double my = motion.y - (double) 0.04F;
        double mz = motion.z;
        double posX = cart.getX();
        double posY = cart.getY() + Y_OFFSET;
        double posZ = cart.getZ();
        int x = Mth.floor(posX);
        int y = Mth.floor(posY);
        int z = Mth.floor(posZ);
        if (isRail(level, x, y - 1, z)) {
            y--;
        }
        double maxSpeed = 0.4D;
        boolean pushing = false;
        double slope = 0.0078125D;
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        Entity rider = cart.getFirstPassenger();
        MinecartFurnace furnace = cart instanceof MinecartFurnace f ? f : null;
        if (BaseRailBlock.isRail(state)) {
            Vec3 before = railPos(level, posX, posY, posZ);
            int meta = shape(state);
            posY = y;
            boolean powered = false;
            boolean braking = false;
            if (state.is(Blocks.POWERED_RAIL)) {
                powered = state.getValue(PoweredRailBlock.POWERED);
                braking = !powered;
            }
            if (meta >= 2 && meta <= 5) {
                posY = y + 1;
            }
            if (meta == 2) {
                mx -= slope;
            }
            if (meta == 3) {
                mx += slope;
            }
            if (meta == 4) {
                mz += slope;
            }
            if (meta == 5) {
                mz -= slope;
            }
            int[][] exits = EXITS[meta];
            double dx = exits[1][0] - exits[0][0];
            double dz = exits[1][2] - exits[0][2];
            double length = Math.sqrt(dx * dx + dz * dz);
            if (mx * dx + mz * dz < 0.0D) {
                dx = -dx;
                dz = -dz;
            }
            double speed = Math.sqrt(mx * mx + mz * mz);
            mx = speed * dx / length;
            mz = speed * dz / length;
            if (braking) {
                if (Math.sqrt(mx * mx + mz * mz) < 0.03D) {
                    mx *= 0.0D;
                    my *= 0.0D;
                    mz *= 0.0D;
                } else {
                    mx *= 0.5D;
                    my *= 0.0D;
                    mz *= 0.5D;
                }
            }
            double along;
            double x0 = (double) x + 0.5D + (double) exits[0][0] * 0.5D;
            double z0 = (double) z + 0.5D + (double) exits[0][2] * 0.5D;
            double x1 = (double) x + 0.5D + (double) exits[1][0] * 0.5D;
            double z1 = (double) z + 0.5D + (double) exits[1][2] * 0.5D;
            dx = x1 - x0;
            dz = z1 - z0;
            if (dx == 0.0D) {
                posX = (double) x + 0.5D;
                along = posZ - (double) z;
            } else if (dz == 0.0D) {
                posZ = (double) z + 0.5D;
                along = posX - (double) x;
            } else {
                along = ((posX - x0) * dx + (posZ - z0) * dz) * 2.0D;
            }
            posX = x0 + dx * along;
            posZ = z0 + dz * along;
            cart.setPos(posX, posY, posZ);
            double moveX = mx;
            double moveZ = mz;
            if (rider != null) {
                moveX *= 0.75D;
                moveZ *= 0.75D;
            }
            moveX = Mth.clamp(moveX, -maxSpeed, maxSpeed);
            moveZ = Mth.clamp(moveZ, -maxSpeed, maxSpeed);
            cart.setDeltaMovement(mx, my, mz);
            cart.move(MoverType.SELF, new Vec3(moveX, 0.0D, moveZ));
            motion = cart.getDeltaMovement();
            mx = motion.x;
            my = motion.y;
            mz = motion.z;
            if (exits[0][1] != 0 && Mth.floor(cart.getX()) - x == exits[0][0] && Mth.floor(cart.getZ()) - z == exits[0][2]) {
                cart.setPos(cart.getX(), cart.getY() + (double) exits[0][1], cart.getZ());
            } else if (exits[1][1] != 0 && Mth.floor(cart.getX()) - x == exits[1][0]
                    && Mth.floor(cart.getZ()) - z == exits[1][2]) {
                cart.setPos(cart.getX(), cart.getY() + (double) exits[1][1], cart.getZ());
            }
            if (rider != null) {
                mx *= (double) 0.997F;
                my *= 0.0D;
                mz *= (double) 0.997F;
            } else {
                if (furnace != null) {
                    Vec3 push = furnace.push;
                    double pushX = push.x;
                    double pushZ = push.z;
                    double force = sqrtDouble(pushX * pushX + pushZ * pushZ);
                    if (force > 0.01D) {
                        pushing = true;
                        pushX /= force;
                        pushZ /= force;
                        double accel = 0.04D;
                        mx *= (double) 0.8F;
                        my *= 0.0D;
                        mz *= (double) 0.8F;
                        mx += pushX * accel;
                        mz += pushZ * accel;
                    } else {
                        mx *= (double) 0.9F;
                        my *= 0.0D;
                        mz *= (double) 0.9F;
                    }
                    furnace.push = new Vec3(pushX, 0.0D, pushZ);
                }
                mx *= (double) 0.96F;
                my *= 0.0D;
                mz *= (double) 0.96F;
            }
            Vec3 after = railPos(level, cart.getX(), cart.getY() + Y_OFFSET, cart.getZ());
            if (after != null && before != null) {
                double gain = (before.y - after.y) * 0.05D;
                speed = Math.sqrt(mx * mx + mz * mz);
                if (speed > 0.0D) {
                    mx = mx / speed * (speed + gain);
                    mz = mz / speed * (speed + gain);
                }
                cart.setPos(cart.getX(), after.y - Y_OFFSET, cart.getZ());
            }
            int nx = Mth.floor(cart.getX());
            int nz = Mth.floor(cart.getZ());
            if (nx != x || nz != z) {
                speed = Math.sqrt(mx * mx + mz * mz);
                mx = speed * (double) (nx - x);
                mz = speed * (double) (nz - z);
            }
            if (furnace != null) {
                Vec3 push = furnace.push;
                double pushX = push.x;
                double pushZ = push.z;
                double force = sqrtDouble(pushX * pushX + pushZ * pushZ);
                if (force > 0.01D && mx * mx + mz * mz > 0.001D) {
                    pushX /= force;
                    pushZ /= force;
                    if (pushX * mx + pushZ * mz < 0.0D) {
                        pushX = 0.0D;
                        pushZ = 0.0D;
                    } else {
                        pushX = mx;
                        pushZ = mz;
                    }
                    furnace.push = new Vec3(pushX, 0.0D, pushZ);
                }
            }
            if (powered) {
                speed = Math.sqrt(mx * mx + mz * mz);
                if (speed > 0.01D) {
                    double boost = 0.06D;
                    mx += mx / speed * boost;
                    mz += mz / speed * boost;
                } else if (meta == 1) {
                    if (cart.isRedstoneConductor(new BlockPos(x - 1, y, z))) {
                        mx = 0.02D;
                    } else if (cart.isRedstoneConductor(new BlockPos(x + 1, y, z))) {
                        mx = -0.02D;
                    }
                } else if (meta == 0) {
                    if (cart.isRedstoneConductor(new BlockPos(x, y, z - 1))) {
                        mz = 0.02D;
                    } else if (cart.isRedstoneConductor(new BlockPos(x, y, z + 1))) {
                        mz = -0.02D;
                    }
                }
            }
            cart.setOnRails(true);
        } else {
            cart.setOnRails(false);
            mx = Mth.clamp(mx, -maxSpeed, maxSpeed);
            mz = Mth.clamp(mz, -maxSpeed, maxSpeed);
            if (cart.onGround()) {
                mx *= 0.5D;
                my *= 0.5D;
                mz *= 0.5D;
            }
            cart.setDeltaMovement(mx, my, mz);
            cart.move(MoverType.SELF, new Vec3(mx, my, mz));
            motion = cart.getDeltaMovement();
            mx = motion.x;
            my = motion.y;
            mz = motion.z;
            if (!cart.onGround()) {
                mx *= (double) 0.95F;
                my *= (double) 0.95F;
                mz *= (double) 0.95F;
            }
        }
        cart.setDeltaMovement(mx, my, mz);
        face(cart);
        AABB reach = cart.getBoundingBox().inflate((double) 0.2F, 0.0D, (double) 0.2F);
        for (Entity other : level.getEntities(cart, reach)) {
            if (other != rider && other.isPushable() && other instanceof AbstractMinecart otherCart) {
                collide(otherCart, cart);
            }
        }
        if (furnace != null && pushing && cart.getRandom().nextInt(4) == 0) {
            MinecartFurnaceAccessor fuel = (MinecartFurnaceAccessor) furnace;
            fuel.b173$setFuel(fuel.b173$getFuel() - 1);
            if (fuel.b173$getFuel() < 0) {
                furnace.push = Vec3.ZERO;
            }
            level.sendParticles(ParticleTypes.LARGE_SMOKE, cart.getX(), cart.getY() + Y_OFFSET + 0.8D, cart.getZ(), 0,
                    0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void face(AbstractMinecart cart) {
        float yaw = cart.getYRot();
        double backX = cart.xo - cart.getX();
        double backZ = cart.zo - cart.getZ();
        if (backX * backX + backZ * backZ > 0.001D) {
            yaw = (float) (Math.atan2(backZ, backX) * 180.0D / Math.PI);
            if (cart.isFlipped()) {
                yaw += 180.0F;
            }
        }
        double turn = (double) (yaw - cart.yRotO);
        while (turn >= 180.0D) {
            turn -= 360.0D;
        }
        while (turn < -180.0D) {
            turn += 360.0D;
        }
        if (turn < -170.0D || turn >= 170.0D) {
            yaw += 180.0F;
            cart.setFlipped(!cart.isFlipped());
        }
        cart.setYRot(yaw % 360.0F);
        cart.setXRot(0.0F);
    }

    public static Vec3 railPos(Level level, double posX, double posY, double posZ) {
        int x = Mth.floor(posX);
        int y = Mth.floor(posY);
        int z = Mth.floor(posZ);
        if (isRail(level, x, y - 1, z)) {
            y--;
        }
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        if (!BaseRailBlock.isRail(state)) {
            return null;
        }
        int[][] exits = EXITS[shape(state)];
        double along;
        double x0 = (double) x + 0.5D + (double) exits[0][0] * 0.5D;
        double y0 = (double) y + 0.5D + (double) exits[0][1] * 0.5D;
        double z0 = (double) z + 0.5D + (double) exits[0][2] * 0.5D;
        double x1 = (double) x + 0.5D + (double) exits[1][0] * 0.5D;
        double y1 = (double) y + 0.5D + (double) exits[1][1] * 0.5D;
        double z1 = (double) z + 0.5D + (double) exits[1][2] * 0.5D;
        double dx = x1 - x0;
        double dy = (y1 - y0) * 2.0D;
        double dz = z1 - z0;
        if (dx == 0.0D) {
            posX = (double) x + 0.5D;
            along = posZ - (double) z;
        } else if (dz == 0.0D) {
            posZ = (double) z + 0.5D;
            along = posX - (double) x;
        } else {
            along = ((posX - x0) * dx + (posZ - z0) * dz) * 2.0D;
        }
        posX = x0 + dx * along;
        posY = y0 + dy * along;
        posZ = z0 + dz * along;
        if (dy < 0.0D) {
            posY += 1.0D;
        }
        if (dy > 0.0D) {
            posY += 0.5D;
        }
        return new Vec3(posX, posY, posZ);
    }

    public static final int DISPLAY_OFFSET = 5;

    private static Vec3 shove(AbstractMinecart self, Entity entity) {
        double dx = entity.getX() - self.getX();
        double dz = entity.getZ() - self.getZ();
        double distance = dx * dx + dz * dz;
        if (distance < (double) 1.0E-4F) {
            return null;
        }
        distance = sqrtDouble(distance);
        dx /= distance;
        dz /= distance;
        double near = 1.0D / distance;
        if (near > 1.0D) {
            near = 1.0D;
        }
        dx *= near;
        dz *= near;
        dx *= (double) 0.1F;
        dz *= (double) 0.1F;
        dx *= 0.5D;
        dz *= 0.5D;
        return new Vec3(dx, 0.0D, dz);
    }

    public static void pushBack(AbstractMinecart self, Entity entity) {
        if (self.hasPassenger(entity)) {
            return;
        }
        Vec3 shove = shove(self, entity);
        if (shove != null) {
            entity.push(shove.x / 4.0D, 0.0D, shove.z / 4.0D);
        }
    }

    public static void collide(AbstractMinecart self, Entity entity) {
        if (self.hasPassenger(entity)) {
            return;
        }
        Vec3 own = self.getDeltaMovement();
        if (entity instanceof LivingEntity && !(entity instanceof Player) && self.isRideable()
                && own.x * own.x + own.z * own.z > 0.01D && !self.isVehicle() && !entity.isPassenger()) {
            entity.startRiding(self, true, true);
        }
        Vec3 shove = shove(self, entity);
        if (shove == null) {
            return;
        }
        double dx = shove.x;
        double dz = shove.z;
        if (entity instanceof AbstractMinecart other) {
            double offX = entity.getX() - self.getX();
            double offZ = entity.getZ() - self.getZ();
            Vec3 theirs = other.getDeltaMovement();
            double slip = offX * theirs.z + offZ * other.xo;
            slip *= slip;
            if (slip > 5.0D) {
                return;
            }
            double sumX = theirs.x + own.x;
            double sumZ = theirs.z + own.z;
            if (other.isFurnace() && !self.isFurnace()) {
                self.setDeltaMovement(own.x * (double) 0.2F + theirs.x - dx, own.y,
                        own.z * (double) 0.2F + theirs.z - dz);
                other.setDeltaMovement(theirs.x * (double) 0.7F, theirs.y, theirs.z * (double) 0.7F);
            } else if (!other.isFurnace() && self.isFurnace()) {
                other.setDeltaMovement(theirs.x * (double) 0.2F + own.x + dx, theirs.y,
                        theirs.z * (double) 0.2F + own.z + dz);
                self.setDeltaMovement(own.x * (double) 0.7F, own.y, own.z * (double) 0.7F);
            } else {
                sumX /= 2.0D;
                sumZ /= 2.0D;
                self.setDeltaMovement(own.x * (double) 0.2F + sumX - dx, own.y, own.z * (double) 0.2F + sumZ - dz);
                other.setDeltaMovement(theirs.x * (double) 0.2F + sumX + dx, theirs.y,
                        theirs.z * (double) 0.2F + sumZ + dz);
            }
        } else {
            self.setDeltaMovement(own.x - dx, own.y, own.z - dz);
            entity.push(dx / 4.0D, 0.0D, dz / 4.0D);
        }
        self.needsSync = true;
    }

    public static void dropCart(ServerLevel level, Entity cart) {
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }
        cart.spawnAtLocation(level, new ItemStack(Items.MINECART), (float) Y_OFFSET);
        if (cart instanceof MinecartChest) {
            cart.spawnAtLocation(level, new ItemStack(Items.CHEST), (float) Y_OFFSET);
        } else if (cart instanceof MinecartFurnace) {
            cart.spawnAtLocation(level, new ItemStack(Items.FURNACE), (float) Y_OFFSET);
        }
    }

    public static boolean fuel(MinecartFurnace furnace, Vec3 playerPos, ItemStack stack) {
        boolean coal = stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
        if (coal) {
            MinecartFurnaceAccessor fuel = (MinecartFurnaceAccessor) furnace;
            fuel.b173$setFuel(fuel.b173$getFuel() + COAL_FUEL);
        }
        furnace.push = new Vec3(furnace.getX() - playerPos.x, 0.0D, furnace.getZ() - playerPos.z);
        return coal;
    }
}
