package dev.b173.rules;

import dev.b173.core.BetaSky;
import dev.b173.world.BetaBiomeSource;
import dev.b173.world.BetaChunkGenerator;
import dev.b173.world.BetaNetherGenerator;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.List;

public final class BetaSpawner {
    private static final MobCategory[] CATEGORIES = {MobCategory.MONSTER, MobCategory.CREATURE,
            MobCategory.WATER_CREATURE};
    private static final int[] CAPS = {70, 15, 5};
    private static final int CHUNK_RADIUS = 8;
    private static final double PLAYER_CLEARANCE = 24.0D;
    private static final float SPAWN_CLEARANCE_SQUARED = 576.0F;
    private static final long SLIME_SALT = 987234911L;

    private static final LongOpenHashSet ELIGIBLE = new LongOpenHashSet();
    private static final Long2ObjectOpenHashMap<Holder<Biome>> BIOMES = new Long2ObjectOpenHashMap<>();
    private static Object biomesFor;

    private BetaSpawner() {
    }

    public static boolean runsIn(ServerLevel level) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        return generator instanceof BetaChunkGenerator || generator instanceof BetaNetherGenerator;
    }

    public static void tick(ServerLevel level, boolean monsters) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        BetaChunkGenerator overworld = null;
        BetaBiomeSource biomes = null;
        Holder<Biome> fixed = null;
        if (generator instanceof BetaChunkGenerator beta && beta.getBiomeSource() instanceof BetaBiomeSource source) {
            overworld = beta;
            biomes = source;
        } else if (generator instanceof BetaNetherGenerator nether
                && nether.getBiomeSource().possibleBiomes().size() == 1) {
            fixed = nether.getBiomeSource().possibleBiomes().iterator().next();
        } else {
            return;
        }
        collectEligibleChunks(level);
        if (ELIGIBLE.isEmpty()) {
            return;
        }
        if (overworld != null && (biomesFor != overworld.climate() || BIOMES.size() > 65536)) {
            BIOMES.clear();
            biomesFor = overworld.climate();
        }
        int[] counts = countCreatures(level);
        Spot spot = new Spot(level);
        for (int kind = 0; kind < CATEGORIES.length; kind++) {
            if (kind == 0 && !monsters) {
                continue;
            }
            if (counts[kind] > CAPS[kind] * ELIGIBLE.size() / 256) {
                continue;
            }
            for (LongIterator chunks = ELIGIBLE.iterator(); chunks.hasNext(); ) {
                long chunk = chunks.nextLong();
                Holder<Biome> biome = fixed;
                if (biome == null) {
                    BetaChunkGenerator beta = overworld;
                    BetaBiomeSource source = biomes;
                    biome = BIOMES.computeIfAbsent(chunk, key -> source.holder(
                            beta.climate().biomeAt(ChunkPos.getX(key) << 4, ChunkPos.getZ(key) << 4)));
                }
                spawnInChunk(spot, kind, biome, ChunkPos.getX(chunk), ChunkPos.getZ(chunk));
            }
        }
    }

    private static void collectEligibleChunks(ServerLevel level) {
        ELIGIBLE.clear();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            int playerX = Mth.floor(player.getX() / 16.0D);
            int playerZ = Mth.floor(player.getZ() / 16.0D);
            for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                    long chunk = ChunkPos.pack(playerX + dx, playerZ + dz);
                    if (!ELIGIBLE.contains(chunk)
                            && level.canSpawnEntitiesInChunk(new ChunkPos(playerX + dx, playerZ + dz))
                            && level.getChunkSource().getChunkNow(playerX + dx, playerZ + dz) != null) {
                        ELIGIBLE.add(chunk);
                    }
                }
            }
        }
    }

    private static int[] countCreatures(ServerLevel level) {
        int[] counts = new int[CATEGORIES.length];
        for (Entity entity : level.getAllEntities()) {
            MobCategory category = entity.getType().getCategory();
            for (int kind = 0; kind < CATEGORIES.length; kind++) {
                if (category == CATEGORIES[kind]) {
                    counts[kind]++;
                    break;
                }
            }
        }
        return counts;
    }

    private static void spawnInChunk(Spot spot, int kind, Holder<Biome> biome, int chunkX, int chunkZ) {
        MobSpawnSettings settings = biome.value().getMobSettings();
        List<Weighted<MobSpawnSettings.SpawnerData>> list = settings.getMobs(CATEGORIES[kind]).unwrap();
        if (list.isEmpty()) {
            return;
        }
        RandomSource random = spot.random;
        int totalWeight = 0;
        for (Weighted<MobSpawnSettings.SpawnerData> entry : list) {
            totalWeight += entry.weight();
        }
        if (totalWeight <= 0) {
            return;
        }
        int roll = random.nextInt(totalWeight);
        EntityType<?> type = list.getFirst().value().type();
        for (Weighted<MobSpawnSettings.SpawnerData> entry : list) {
            roll -= entry.weight();
            if (roll < 0) {
                type = entry.value().type();
                break;
            }
        }
        int x = (chunkX << 4) + random.nextInt(16);
        int y = random.nextInt(128);
        int z = (chunkZ << 4) + random.nextInt(16);
        BlockState start = spot.state(x, y, z);
        if (start == null || spot.normalCube(start, x, y, z)) {
            return;
        }
        boolean water = CATEGORIES[kind] == MobCategory.WATER_CREATURE;
        if (water ? !start.is(Blocks.WATER) : !start.isAir()) {
            return;
        }
        int spawned = 0;
        int limit = BetaMobs.maxPerChunk(type);
        for (int pack = 0; pack < 3; pack++) {
            int px = x;
            int py = y;
            int pz = z;
            for (int step = 0; step < 4; step++) {
                px += random.nextInt(6) - random.nextInt(6);
                py += random.nextInt(1) - random.nextInt(1);
                pz += random.nextInt(6) - random.nextInt(6);
                if (!spot.groundSuits(water, px, py, pz)) {
                    continue;
                }
                float fx = (float) px + 0.5F;
                float fy = (float) py;
                float fz = (float) pz + 0.5F;
                if (spot.level.getNearestPlayer(fx, fy, fz, PLAYER_CLEARANCE, EntitySelector.NO_SPECTATORS) != null
                        || spot.nearWorldSpawn(fx, fy, fz)) {
                    continue;
                }
                float yaw = random.nextFloat() * 360.0F;
                int slimeSize = type == EntityType.SLIME ? 1 << random.nextInt(3) : 0;
                if (!spot.canSpawnHere(type, CATEGORIES[kind], fx, fy, fz, px, py, pz, slimeSize)) {
                    continue;
                }
                if (spawn(spot, type, fx, fy, fz, yaw, slimeSize) != null) {
                    spawned++;
                    if (spawned >= limit) {
                        return;
                    }
                }
            }
        }
    }

    static Mob spawn(Spot spot, EntityType<?> type, float x, float y, float z, float yaw, int slimeSize) {
        ServerLevel level = spot.level;
        if (!(type.create(level, EntitySpawnReason.NATURAL) instanceof Mob mob)) {
            return null;
        }
        if (mob instanceof Slime slime) {
            slime.setSize(slimeSize, true);
        }
        mob.snapTo(x, y, z, yaw, 0.0F);
        if (type == EntityType.SKELETON) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        } else if (type == EntityType.ZOMBIFIED_PIGLIN) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        }
        RandomSource random = spot.random;
        if (mob instanceof Sheep sheep) {
            sheep.setColor(fleece(random));
        }
        level.addFreshEntity(mob);
        if (type == EntityType.SPIDER && random.nextInt(100) == 0) {
            Mob skeleton = EntityType.SKELETON.create(level, EntitySpawnReason.JOCKEY);
            if (skeleton != null) {
                skeleton.snapTo(x, y, z, mob.getYRot(), 0.0F);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                level.addFreshEntity(skeleton);
                skeleton.startRiding(mob);
            }
        }
        return mob;
    }

    private static DyeColor fleece(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 5) {
            return DyeColor.BLACK;
        }
        if (roll < 10) {
            return DyeColor.GRAY;
        }
        if (roll < 15) {
            return DyeColor.LIGHT_GRAY;
        }
        if (roll < 18) {
            return DyeColor.BROWN;
        }
        return random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
    }

    static final class Spot {
        final ServerLevel level;
        final RandomSource random;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final int skyDarkening;
        final boolean thundering;
        final boolean peaceful;
        final long seed;
        final boolean hasWorldSpawn;
        final float spawnX;
        final float spawnY;
        final float spawnZ;
        private LevelChunk chunk;
        private int chunkX = Integer.MIN_VALUE;
        private int chunkZ = Integer.MIN_VALUE;

        Spot(ServerLevel level) {
            this.level = level;
            this.random = level.getRandom();
            this.skyDarkening = BetaSky.skyDarkening(level.getOverworldClockTime(), 1.0F,
                    level.getRainLevel(1.0F), level.getThunderLevel(1.0F));
            this.thundering = level.isThundering();
            this.peaceful = level.getDifficulty() == Difficulty.PEACEFUL;
            this.seed = level.getSeed();
            LevelData.RespawnData respawn = level.getRespawnData();
            this.hasWorldSpawn = respawn.dimension() == level.dimension()
                    || level.getChunkSource().getGenerator() instanceof BetaNetherGenerator;
            this.spawnX = respawn.pos().getX();
            this.spawnY = respawn.pos().getY();
            this.spawnZ = respawn.pos().getZ();
        }

        BlockState state(int x, int y, int z) {
            int cx = x >> 4;
            int cz = z >> 4;
            if (cx != chunkX || cz != chunkZ) {
                chunkX = cx;
                chunkZ = cz;
                chunk = level.canSpawnEntitiesInChunk(new ChunkPos(cx, cz))
                        ? level.getChunkSource().getChunkNow(cx, cz) : null;
            }
            if (chunk == null) {
                return null;
            }
            if (level.isOutsideBuildHeight(y)) {
                return Blocks.AIR.defaultBlockState();
            }
            return chunk.getBlockState(pos.set(x, y, z));
        }

        boolean normalCube(BlockState state, int x, int y, int z) {
            return state.isRedstoneConductor(level, pos.set(x, y, z));
        }

        boolean groundSuits(boolean water, int x, int y, int z) {
            BlockState here = state(x, y, z);
            BlockState above = state(x, y + 1, z);
            if (here == null || above == null || normalCube(above, x, y + 1, z)) {
                return false;
            }
            if (water) {
                return !here.getFluidState().isEmpty();
            }
            BlockState below = state(x, y - 1, z);
            return below != null && normalCube(below, x, y - 1, z) && !normalCube(here, x, y, z)
                    && here.getFluidState().isEmpty();
        }

        boolean nearWorldSpawn(float x, float y, float z) {
            if (!hasWorldSpawn) {
                return false;
            }
            float dx = x - spawnX;
            float dy = y - spawnY;
            float dz = z - spawnZ;
            return dx * dx + dy * dy + dz * dz < SPAWN_CLEARANCE_SQUARED;
        }

        boolean canSpawnHere(EntityType<?> type, MobCategory category, float x, float y, float z,
                             int blockX, int blockY, int blockZ, int slimeSize) {
            if (type == EntityType.SLIME) {
                return (slimeSize == 1 || !peaceful) && random.nextInt(10) == 0
                        && WorldgenRandom.seedSlimeChunk(blockX >> 4, blockZ >> 4, seed, SLIME_SALT).nextInt(10) == 0
                        && y < 16.0F;
            }
            if (type == EntityType.GHAST) {
                return random.nextInt(20) == 0 && roomFor(type, x, y, z) && !peaceful;
            }
            if (type == EntityType.ZOMBIFIED_PIGLIN) {
                return !peaceful && roomFor(type, x, y, z);
            }
            if (category == MobCategory.MONSTER) {
                return darkEnough(blockX, blockY, blockZ) && roomFor(type, x, y, z);
            }
            if (category == MobCategory.CREATURE) {
                BlockState below = state(blockX, blockY - 1, blockZ);
                return below != null && below.is(Blocks.GRASS_BLOCK) && fullLight(blockX, blockY, blockZ) > 8
                        && roomFor(type, x, y, z);
            }
            if (category == MobCategory.WATER_CREATURE) {
                return level.isUnobstructed(null, Shapes.create(box(type, x, y, z)));
            }
            return false;
        }

        private boolean darkEnough(int x, int y, int z) {
            pos.set(x, y, z);
            int sky = level.getBrightness(LightLayer.SKY, pos);
            if (sky > random.nextInt(32)) {
                return false;
            }
            int block = level.getBrightness(LightLayer.BLOCK, pos);
            int light = Math.max(sky - (thundering ? 10 : skyDarkening), block);
            if (light > random.nextInt(8)) {
                return false;
            }
            return Math.max(sky - skyDarkening, block) <= 11;
        }

        private int fullLight(int x, int y, int z) {
            pos.set(x, y, z);
            return Math.max(level.getBrightness(LightLayer.SKY, pos), level.getBrightness(LightLayer.BLOCK, pos));
        }

        private boolean roomFor(EntityType<?> type, float x, float y, float z) {
            AABB box = box(type, x, y, z);
            return level.isUnobstructed(null, Shapes.create(box)) && level.noCollision(box)
                    && !level.containsAnyLiquid(box);
        }

        private static AABB box(EntityType<?> type, float x, float y, float z) {
            return type.getDimensions().makeBoundingBox(x, y, z);
        }
    }
}
