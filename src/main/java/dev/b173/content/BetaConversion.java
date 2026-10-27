package dev.b173.content;

import dev.b173.Beta173Mod;
import dev.b173.core.BetaChest;
import dev.b173.core.BetaContent;
import dev.b173.mixin.content.ContainerItemsAccess;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.TagValueInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class BetaConversion {
    private static final Map<BlockState, BlockState> STATES = new ConcurrentHashMap<>();
    private static final Queue<Runnable> AFTER_LOAD = new ConcurrentLinkedQueue<>();
    private static final Set<DataComponentType<?>> BETA_PARTS = Set.of(DataComponents.DAMAGE, DataComponents.MAP_ID,
            DataComponents.MAP_DECORATIONS, DataComponents.MAP_POST_PROCESSING, DataComponents.CUSTOM_DATA);

    private BetaConversion() {
    }

    public static void init() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> convertChunk(level, chunk));
        ServerEntityEvents.ENTITY_LOAD.register(BetaConversion::convertEntity);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> convertPlayer(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(BetaConversion::afterLoads);
    }

    public static BlockState convert(BlockState state) {
        if (Boolean.TRUE.equals(BetaLater.beta(state.getBlock()))) {
            return dry(state);
        }
        return STATES.computeIfAbsent(state, BetaConversion::counterpart);
    }

    private static BlockState dry(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)
                ? state.setValue(BlockStateProperties.WATERLOGGED, false) : state;
    }

    private static BlockState counterpart(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);
        boolean upper = state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
        String target = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("minecraft")
                ? BetaContent.convertBlock(path, waterlogged, upper) : waterlogged ? "water" : "air";
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(BetaContent.blockOf(target)));
        BlockState result = block.defaultBlockState();
        for (Property<?> property : result.getProperties()) {
            result = copy(state, result, property);
        }
        int bracket = target.indexOf('[');
        if (bracket >= 0) {
            for (String pair : target.substring(bracket + 1, target.length() - 1).split(",")) {
                String[] kv = pair.split("=");
                result = set(result, block.getStateDefinition(), kv[0], kv[1]);
            }
        }
        return dry(result);
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to, Property<T> property) {
        for (Property<?> theirs : from.getProperties()) {
            if (theirs.getName().equals(property.getName())) {
                Optional<T> value = property.getValue(valueName(from, theirs));
                if (value.isPresent()) {
                    return to.setValue(property, value.get());
                }
            }
        }
        return to;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static BlockState set(BlockState state, StateDefinition<Block, BlockState> definition, String name,
                                  String value) {
        Property<?> property = definition.getProperty(name);
        return property == null ? state : setNamed(state, property, value);
    }

    private static <T extends Comparable<T>> BlockState setNamed(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }

    private static void convertChunk(ServerLevel level, LevelChunk chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        ChunkPos chunkPos = chunk.getPos();
        Map<String, Integer> blocks = new TreeMap<>();
        List<BlockPos> chests = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int index = 0; index < sections.length; index++) {
            LevelChunkSection section = sections[index];
            if (section.hasOnlyAir()) {
                continue;
            }
            boolean later = section.getStates().maybeHas(state -> convert(state) != state);
            boolean chest = section.getStates().maybeHas(state -> state.is(Blocks.CHEST));
            if (!later && !chest) {
                continue;
            }
            int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        cursor.set(chunkPos.getMinBlockX() + x, baseY + y, chunkPos.getMinBlockZ() + z);
                        if (state.is(Blocks.CHEST)) {
                            chests.add(cursor.immutable());
                            continue;
                        }
                        BlockState target = convert(state);
                        if (target != state) {
                            replace(level, chunk, cursor.immutable(), target);
                            blocks.merge(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath(), 1, Integer::sum);
                            if (target.is(Blocks.CHEST)) {
                                chests.add(cursor.immutable());
                            }
                        }
                    }
                }
            }
        }
        int stacks = 0;
        for (BlockEntity blockEntity : List.copyOf(chunk.getBlockEntities().values())) {
            stacks += convertSlots(level, blockEntity);
        }
        faceChests(level, chunk, chests);
        if (!blocks.isEmpty() || stacks > 0) {
            chunk.markUnsaved();
            StringBuilder what = new StringBuilder();
            blocks.forEach((path, count) -> what.append(what.isEmpty() ? "" : ", ").append(count).append(' ').append(path));
            if (stacks > 0) {
                what.append(what.isEmpty() ? "" : "; ").append(stacks).append(stacks == 1 ? " stack" : " stacks");
            }
            Beta173Mod.LOGGER.info("Later content made Beta's or removed in chunk {} of {}: {}",
                    chunkPos, level.dimension().identifier(), what);
        }
    }

    private static void replace(ServerLevel level, LevelChunk chunk, BlockPos pos, BlockState to) {
        BlockEntity old = chunk.getBlockEntity(pos);
        CompoundTag data = old == null || !to.hasBlockEntity() ? null : old.saveWithoutMetadata(level.registryAccess());
        chunk.setBlockState(pos, to, Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
        BlockEntity now = data == null ? null : chunk.getBlockEntity(pos);
        if (now != null && (now.getClass().isInstance(old) || old.getClass().isInstance(now))) {
            now.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data));
        }
    }

    private static int convertSlots(ServerLevel level, BlockEntity blockEntity) {
        if (blockEntity instanceof BaseContainerBlockEntity container) {
            NonNullList<ItemStack> items = ((ContainerItemsAccess) container).b173$items();
            int changed = 0;
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack stack = items.get(slot);
                ItemStack converted = convert(stack.copy());
                if (!ItemStack.matches(stack, converted)) {
                    items.set(slot, converted);
                    changed++;
                }
            }
            return changed;
        }
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            ItemStack record = jukebox.getTheItem();
            ItemStack converted = convert(record.copy());
            if (!ItemStack.matches(record, converted)) {
                BlockPos pos = blockEntity.getBlockPos();
                AFTER_LOAD.add(() -> {
                    if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null
                            && level.getBlockEntity(pos) instanceof JukeboxBlockEntity still) {
                        still.setTheItem(convert(still.getTheItem().copy()));
                    }
                });
                return 1;
            }
        }
        return 0;
    }

    private static void faceChests(ServerLevel level, LevelChunk chunk, List<BlockPos> chests) {
        BetaChest.Blocks loaded = BetaChests.loadedIds(level);
        for (BlockPos pos : chests) {
            face(level, chunk, pos, loaded, false);
        }
        ChunkPos at = chunk.getPos();
        int[][] sides = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int[] side : sides) {
            LevelChunk other = level.getChunkSource().getChunkNow(at.x() + side[0], at.z() + side[1]);
            if (other == null) {
                continue;
            }
            LevelChunkSection[] sections = other.getSections();
            for (int index = 0; index < sections.length; index++) {
                LevelChunkSection section = sections[index];
                if (section.hasOnlyAir() || !section.getStates().maybeHas(state -> state.is(Blocks.CHEST))) {
                    continue;
                }
                int baseY = other.getSectionYFromSectionIndex(index) << 4;
                for (int y = 0; y < 16; y++) {
                    for (int along = 0; along < 16; along++) {
                        for (int depth = 0; depth < 2; depth++) {
                            int across = side[0] + side[1] < 0 ? 15 - depth : depth;
                            int x = side[0] != 0 ? across : along;
                            int z = side[0] != 0 ? along : across;
                            if (section.getBlockState(x, y, z).is(Blocks.CHEST)) {
                                cursor.set(other.getPos().getMinBlockX() + x, baseY + y,
                                        other.getPos().getMinBlockZ() + z);
                                face(level, other, cursor.immutable(), loaded, true);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void face(ServerLevel level, LevelChunk chunk, BlockPos pos, BetaChest.Blocks loaded, boolean send) {
        BlockState state = chunk.getBlockState(pos);
        BlockState beta = BetaChests.state(loaded, pos, state);
        if (beta != state) {
            chunk.setBlockState(pos, beta, Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
            chunk.markUnsaved();
            if (send) {
                level.getChunkSource().blockChanged(pos);
            }
        }
    }

    private static void afterLoads(MinecraftServer server) {
        for (Runnable task; (task = AFTER_LOAD.poll()) != null; ) {
            task.run();
        }
    }

    public static ItemStack convert(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        Item item = stack.getItem();
        if (Boolean.TRUE.equals(BetaLater.beta(item))) {
            return betaParts(stack);
        }
        Item target = null;
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id.getNamespace().equals("minecraft")) {
            String path = BetaContent.convertItem(id.getPath());
            if (path != null) {
                target = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));
            } else if (item instanceof BlockItem blockItem) {
                Block block = convert(blockItem.getBlock().defaultBlockState()).getBlock();
                if (block != Blocks.AIR && block != Blocks.WATER) {
                    target = block.asItem();
                }
            }
        }
        if (target == null || target == Items.AIR || !Boolean.TRUE.equals(BetaLater.beta(target))) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(target, Math.min(stack.getCount(), target.getDefaultMaxStackSize()));
    }

    private static ItemStack betaParts(ItemStack stack) {
        DataComponentPatch patch = stack.getComponentsPatch();
        DataComponentPatch kept = patch.forget(type -> !BETA_PARTS.contains(type));
        return kept.size() == patch.size() ? stack : new ItemStack(stack.typeHolder(), stack.getCount(), kept);
    }

    public static int convertContainer(Container container) {
        int changed = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            ItemStack converted = convert(stack.copy());
            if (!ItemStack.matches(stack, converted)) {
                container.setItem(slot, converted);
                changed++;
            }
        }
        return changed;
    }

    private static void convertEntity(Entity entity, ServerLevel level) {
        if (entity instanceof ItemEntity item) {
            ItemStack converted = convert(item.getItem().copy());
            if (converted.isEmpty()) {
                item.discard();
            } else if (!ItemStack.matches(item.getItem(), converted)) {
                item.setItem(converted);
            }
        } else if (entity instanceof Container container
                && !(entity instanceof ContainerEntity unopened && unopened.getContainerLootTable() != null)) {
            convertContainer(container);
        }
    }

    private static void convertPlayer(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        int changed = convertContainer(inventory);
        changed += player.getEnderChestInventory().isEmpty() ? 0 : 1;
        player.getEnderChestInventory().clearContent();
        if (changed > 0) {
            Beta173Mod.LOGGER.info("Later content carried by {}: {} stacks made Beta's or removed",
                    player.getName().getString(), changed);
        }
    }
}
