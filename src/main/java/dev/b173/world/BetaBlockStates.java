package dev.b173.world;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BetaBlockStates {
    private BetaBlockStates() {
    }

    public static int pack(int id, int meta) {
        return id | meta << 8;
    }

    public static int id(int packed) {
        return packed & 255;
    }

    public static int meta(int packed) {
        return packed >>> 8 & 15;
    }

    public static BlockState modern(int id, int meta) {
        BlockState state = Tables.MODERN[(id & 255) << 4 | meta & 15];
        return state != null ? state : Tables.MODERN[BetaBlocks.STONE << 4];
    }

    @SuppressWarnings("deprecation")
    public static int beta(BlockState state) {
        int packed = Tables.BETA.getInt(state);
        if (packed >= 0) {
            return packed;
        }
        if (state.isAir()) {
            return BetaBlocks.AIR;
        }
        return state.isSolid() ? pack(BetaBlocks.BEDROCK, 0) : pack(BetaBlocks.TORCH, 0);
    }

    public static Direction pumpkinFacing(int meta) {
        return switch (meta & 3) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }

    private static int pumpkinMeta(Direction facing) {
        return switch (facing) {
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }

    private static final class Tables {
        static final BlockState[] MODERN = new BlockState[256 * 16];
        static final Reference2IntOpenHashMap<BlockState> BETA = new Reference2IntOpenHashMap<>();

        static {
            BETA.defaultReturnValue(-1);
            simple(BetaBlocks.AIR, Blocks.AIR);
            reverse(Blocks.CAVE_AIR, BetaBlocks.AIR, 0);
            reverse(Blocks.VOID_AIR, BetaBlocks.AIR, 0);
            simple(BetaBlocks.STONE, Blocks.STONE);
            simple(BetaBlocks.GRASS, Blocks.GRASS_BLOCK);
            simple(BetaBlocks.DIRT, Blocks.DIRT);
            simple(BetaBlocks.COBBLESTONE, Blocks.COBBLESTONE);
            simple(BetaBlocks.PLANKS, Blocks.OAK_PLANKS);
            simple(BetaBlocks.BEDROCK, Blocks.BEDROCK);
            simple(BetaBlocks.SAND, Blocks.SAND);
            simple(BetaBlocks.GRAVEL, Blocks.GRAVEL);
            simple(BetaBlocks.GOLD_ORE, Blocks.GOLD_ORE);
            simple(BetaBlocks.IRON_ORE, Blocks.IRON_ORE);
            simple(BetaBlocks.COAL_ORE, Blocks.COAL_ORE);
            simple(BetaBlocks.GLASS, Blocks.GLASS);
            simple(BetaBlocks.LAPIS_ORE, Blocks.LAPIS_ORE);
            simple(BetaBlocks.SANDSTONE, Blocks.SANDSTONE);
            simple(BetaBlocks.DEAD_BUSH, Blocks.DEAD_BUSH);
            simple(BetaBlocks.DANDELION, Blocks.DANDELION);
            simple(BetaBlocks.ROSE, Blocks.POPPY);
            simple(BetaBlocks.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM);
            simple(BetaBlocks.RED_MUSHROOM, Blocks.RED_MUSHROOM);
            simple(BetaBlocks.MOSSY_COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
            simple(BetaBlocks.OBSIDIAN, Blocks.OBSIDIAN);
            simple(BetaBlocks.TORCH, Blocks.TORCH);
            reverse(Blocks.WALL_TORCH, BetaBlocks.TORCH, 0);
            simple(BetaBlocks.FIRE, Blocks.FIRE);
            simple(BetaBlocks.SPAWNER, Blocks.SPAWNER);
            simple(BetaBlocks.CHEST, Blocks.CHEST);
            simple(BetaBlocks.DIAMOND_ORE, Blocks.DIAMOND_ORE);
            simple(BetaBlocks.FARMLAND, Blocks.FARMLAND);
            simple(BetaBlocks.REDSTONE_ORE, Blocks.REDSTONE_ORE);
            simple(BetaBlocks.SNOW_LAYER, Blocks.SNOW);
            simple(BetaBlocks.ICE, Blocks.ICE);
            simple(BetaBlocks.SNOW_BLOCK, Blocks.SNOW_BLOCK);
            simple(BetaBlocks.CACTUS, Blocks.CACTUS);
            simple(BetaBlocks.CLAY, Blocks.CLAY);
            simple(BetaBlocks.REEDS, Blocks.SUGAR_CANE);
            simple(BetaBlocks.NETHERRACK, Blocks.NETHERRACK);
            simple(BetaBlocks.SOUL_SAND, Blocks.SOUL_SAND);
            simple(BetaBlocks.GLOWSTONE, Blocks.GLOWSTONE);
            simple(BetaBlocks.PORTAL, Blocks.NETHER_PORTAL);

            Block[] saplings = {Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING};
            Block[] logs = {Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG};
            Block[] leaves = {Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES};
            for (int meta = 0; meta < 16; meta++) {
                int type = (meta & 3) == 3 ? BetaBlocks.WOOD_OAK : meta & 3;
                MODERN[BetaBlocks.SAPLING << 4 | meta] = saplings[type].defaultBlockState();
                MODERN[BetaBlocks.LOG << 4 | meta] = logs[type].defaultBlockState();
                MODERN[BetaBlocks.LEAVES << 4 | meta] = leaves[type].defaultBlockState()
                        .setValue(LeavesBlock.PERSISTENT, false)
                        .setValue(LeavesBlock.DISTANCE, 1);
            }
            for (int type = 0; type < 3; type++) {
                reverse(saplings[type], BetaBlocks.SAPLING, type);
                reverse(logs[type], BetaBlocks.LOG, type);
                reverse(leaves[type], BetaBlocks.LEAVES, type);
            }

            for (int meta = 0; meta < 16; meta++) {
                MODERN[BetaBlocks.TALL_GRASS << 4 | meta] = switch (meta & 3) {
                    case BetaBlocks.TALL_GRASS_SHRUB -> Blocks.DEAD_BUSH.defaultBlockState();
                    case BetaBlocks.TALL_GRASS_FERN -> Blocks.FERN.defaultBlockState();
                    default -> Blocks.SHORT_GRASS.defaultBlockState();
                };
            }
            reverse(Blocks.SHORT_GRASS, BetaBlocks.TALL_GRASS, BetaBlocks.TALL_GRASS_GRASS);
            reverse(Blocks.FERN, BetaBlocks.TALL_GRASS, BetaBlocks.TALL_GRASS_FERN);

            liquid(Blocks.WATER, BetaBlocks.FLOWING_WATER, BetaBlocks.WATER);
            liquid(Blocks.LAVA, BetaBlocks.FLOWING_LAVA, BetaBlocks.LAVA);

            for (int meta = 0; meta < 16; meta++) {
                MODERN[BetaBlocks.PUMPKIN << 4 | meta] = Blocks.CARVED_PUMPKIN.defaultBlockState()
                        .setValue(CarvedPumpkinBlock.FACING, pumpkinFacing(meta));
            }
            for (BlockState state : Blocks.CARVED_PUMPKIN.getStateDefinition().getPossibleStates()) {
                BETA.put(state, pack(BetaBlocks.PUMPKIN, pumpkinMeta(state.getValue(CarvedPumpkinBlock.FACING))));
            }
            reverse(Blocks.PUMPKIN, BetaBlocks.PUMPKIN, 0);

            for (String line : BetaContent.BLOCKS) {
                String[] parts = line.split(" ");
                int id = Integer.parseInt(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    Block block = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(parts[i]));
                    for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                        BETA.putIfAbsent(state, pack(id, 0));
                    }
                }
            }
            Block[] slabs = {Blocks.SMOOTH_STONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.OAK_SLAB, Blocks.COBBLESTONE_SLAB};
            for (int kind = 0; kind < slabs.length; kind++) {
                for (BlockState state : slabs[kind].getStateDefinition().getPossibleStates()) {
                    boolean doubled = state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;
                    BETA.put(state, pack(doubled ? 43 : 44, kind));
                }
            }
            Block[] wool = {Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL,
                    Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL,
                    Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL, Blocks.BROWN_WOOL, Blocks.GREEN_WOOL,
                    Blocks.RED_WOOL, Blocks.BLACK_WOOL};
            for (int colour = 0; colour < wool.length; colour++) {
                reverse(wool[colour], 35, colour);
            }
            lit(Blocks.FURNACE, BlockStateProperties.LIT, 61, 62);
            lit(Blocks.REDSTONE_ORE, BlockStateProperties.LIT, BetaBlocks.REDSTONE_ORE, 74);
            lit(Blocks.REDSTONE_TORCH, BlockStateProperties.LIT, 75, 76);
            lit(Blocks.REDSTONE_WALL_TORCH, BlockStateProperties.LIT, 75, 76);
            lit(Blocks.REPEATER, BlockStateProperties.POWERED, 93, 94);
        }

        private static void lit(Block block, net.minecraft.world.level.block.state.properties.BooleanProperty on,
                                int offId, int onId) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                BETA.put(state, pack(state.getValue(on) ? onId : offId, 0));
            }
        }

        private static void simple(int id, Block block) {
            BlockState state = block.defaultBlockState();
            for (int meta = 0; meta < 16; meta++) {
                if (MODERN[id << 4 | meta] == null) {
                    MODERN[id << 4 | meta] = state;
                }
            }
            reverse(block, id, 0);
        }

        private static void reverse(Block block, int id, int meta) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                BETA.put(state, pack(id, meta));
            }
        }

        private static void liquid(Block block, int flowingId, int stillId) {
            for (int meta = 0; meta < 16; meta++) {
                BlockState state = block.defaultBlockState().setValue(LiquidBlock.LEVEL, meta);
                MODERN[flowingId << 4 | meta] = state;
                MODERN[stillId << 4 | meta] = state;
            }
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                int level = state.getValue(LiquidBlock.LEVEL);
                BETA.put(state, level == 0 ? pack(stillId, 0) : pack(flowingId, level));
            }
        }
    }
}
