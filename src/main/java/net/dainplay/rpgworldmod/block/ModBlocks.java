package net.dainplay.rpgworldmod.block;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.custom.ArborFuelCauldron;
import net.dainplay.rpgworldmod.block.custom.BlowerBlock;
import net.dainplay.rpgworldmod.block.custom.DrillTuskBlock;
import net.dainplay.rpgworldmod.block.custom.FairapierPlantBlock;
import net.dainplay.rpgworldmod.block.custom.FairapierWiltedPlantBlock;
import net.dainplay.rpgworldmod.block.custom.FireCatcherBlock;
import net.dainplay.rpgworldmod.block.custom.FuelLiquidBlock;
import net.dainplay.rpgworldmod.block.custom.GlossomBlock;
import net.dainplay.rpgworldmod.block.custom.HoltsReflectionBlock;
import net.dainplay.rpgworldmod.block.custom.ModCeilingHangingSignBlock;
import net.dainplay.rpgworldmod.block.custom.ModFlammableRotatedPillarBlock;
import net.dainplay.rpgworldmod.block.custom.ModStandingSignBlock;
import net.dainplay.rpgworldmod.block.custom.ModWallHangingSignBlock;
import net.dainplay.rpgworldmod.block.custom.ModWallSignBlock;
import net.dainplay.rpgworldmod.block.custom.MosquitosBlock;
import net.dainplay.rpgworldmod.block.custom.ParalilyBlock;
import net.dainplay.rpgworldmod.block.custom.QuartziteBlock;
import net.dainplay.rpgworldmod.block.custom.QuartziteDrillTuskBlock;
import net.dainplay.rpgworldmod.block.custom.QuartziteSlabBlock;
import net.dainplay.rpgworldmod.block.custom.QuartziteWallBlock;
import net.dainplay.rpgworldmod.block.custom.RazorleafBudBlock;
import net.dainplay.rpgworldmod.block.custom.ShiveralisPlantBlock;
import net.dainplay.rpgworldmod.block.custom.SpikyIvyBlock;
import net.dainplay.rpgworldmod.block.custom.StareblossomBlock;
import net.dainplay.rpgworldmod.block.custom.StareblossomPotBlock;
import net.dainplay.rpgworldmod.block.custom.TreeHollowBlock;
import net.dainplay.rpgworldmod.block.custom.TyphonBlock;
import net.dainplay.rpgworldmod.block.custom.WidoweedBlock;
import net.dainplay.rpgworldmod.block.custom.YoungRazorleafBlock;
import net.dainplay.rpgworldmod.block.entity.ModWoodTypes;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.fluid.ModFluids;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.TooltipBlockItem;
import net.dainplay.rpgworldmod.world.feature.tree.RieTreeGrower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static net.dainplay.rpgworldmod.block.custom.GlossomBlock.GLOWING;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS =
			DeferredRegister.create(ForgeRegistries.BLOCKS, RPGworldMod.MOD_ID);


	public static final RegistryObject<Block> DRILL_TUSK = registerBlock("drill_tusk",
			() -> new DrillTuskBlock(BlockBehaviour.Properties.copy(Blocks.BONE_BLOCK).noOcclusion()));
	public static final RegistryObject<Block> QUARTZITE_DRILL_TUSK = registerBlockWithTooltip("quartzite_drill_tusk",
			() -> new QuartziteDrillTuskBlock(BlockBehaviour.Properties.copy(Blocks.BONE_BLOCK).mapColor(MapColor.QUARTZ).sound(SoundType.STONE).noOcclusion()));
	public static final RegistryObject<Block> BLOWER = registerFuelBlockWithTooltip("blower",
			() -> new BlowerBlock(BlockBehaviour.Properties.copy(Blocks.PISTON).sound(SoundType.WOOD).strength(2.0f).mapColor(MapColor.GLOW_LICHEN)) {
				@Override
				public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return true;
				}

				@Override
				public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 20;
				}

				@Override
				public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 5;
				}
			}, 300);
	public static final RegistryObject<Block> FIRE_CATCHER = BLOCKS.register("fire_catcher",
			() -> new FireCatcherBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)
					.mapColor(MapColor.CRIMSON_NYLIUM)
					.noOcclusion()
					.noCollission()
					.lightLevel(state -> state.getValue(FireCatcherBlock.HUNGRY) ? 7 : 0)
					.pushReaction(PushReaction.DESTROY)));
	public static final RegistryObject<Block> JASPER = registerBlock("jasper",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_RED).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> EMULSION_BLOCK = registerBlock("emulsion_block",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS).strength(0F).mapColor(MapColor.COLOR_MAGENTA).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion()));

	public static final RegistryObject<Block> QUARTZITE = registerBlock("quartzite",
			() -> new QuartziteBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> QUARTZITE_STAIRS = registerBlock("quartzite_stairs",
			() -> new StairBlock(() -> ModBlocks.QUARTZITE.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(Blocks.STONE_STAIRS).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> QUARTZITE_SLAB = registerBlock("quartzite_slab",
			() -> new QuartziteSlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_SLAB).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> QUARTZITE_WALL = registerBlock("quartzite_wall",
			() -> new QuartziteWallBlock(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE_WALL).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> QUARTZITE_BRICKS = registerBlock("quartzite_bricks",
			() -> new QuartziteBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> QUARTZITE_BRICK_STAIRS = registerBlock("quartzite_brick_stairs",
			() -> new StairBlock(() -> ModBlocks.QUARTZITE_BRICKS.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(Blocks.STONE_STAIRS).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> QUARTZITE_BRICK_SLAB = registerBlock("quartzite_brick_slab",
			() -> new QuartziteSlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_SLAB).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> POLISHED_QUARTZITE = registerBlock("polished_quartzite",
			() -> new QuartziteBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> POLISHED_QUARTZITE_STAIRS = registerBlock("polished_quartzite_stairs",
			() -> new StairBlock(() -> ModBlocks.POLISHED_QUARTZITE.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(Blocks.STONE_STAIRS).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> POLISHED_QUARTZITE_SLAB = registerBlock("polished_quartzite_slab",
			() -> new QuartziteSlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_SLAB).mapColor(MapColor.QUARTZ).strength(1.6f).requiresCorrectToolForDrops()));


	public static final RegistryObject<Block> MASKONITE_BLOCK = registerBlock("maskonite_block",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_STAIRS = registerBlock("maskonite_stairs",
			() -> new StairBlock(() -> ModBlocks.MASKONITE_BLOCK.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(Blocks.STONE_STAIRS).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_SLAB = registerBlock("maskonite_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_SLAB).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_WALL = registerBlock("maskonite_wall",
			() -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE_WALL).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_BUTTON = registerBlock("maskonite_button",
			() -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BUTTON).mapColor(MapColor.TERRACOTTA_CYAN).noCollission().strength(0.5F), BlockSetType.STONE, 20, false));
	public static final RegistryObject<Block> MASKONITE_PRESSURE_PLATE = registerBlock("maskonite_pressure_plate",
			() -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.MOBS, BlockBehaviour.Properties.copy(Blocks.STONE_PRESSURE_PLATE).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops(), BlockSetType.STONE));
	public static final RegistryObject<Block> MASKONITE_BRICKS = registerBlock("maskonite_bricks",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_BRICK_STAIRS = registerBlock("maskonite_brick_stairs",
			() -> new StairBlock(() -> ModBlocks.MASKONITE_BRICKS.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_STAIRS).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_BRICK_SLAB = registerBlock("maskonite_brick_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_SLAB).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_BRICK_WALL = registerBlock("maskonite_brick_wall",
			() -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICK_WALL).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> CRACKED_MASKONITE_BRICKS = registerBlock("cracked_maskonite_bricks",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.CRACKED_STONE_BRICKS).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> CHISELED_MASKONITE_BLOCK = registerBlock("chiseled_maskonite_block",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.CHISELED_STONE_BRICKS).mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6f).requiresCorrectToolForDrops()));
	public static final RegistryObject<Block> MASKONITE_GLASS = registerBlock("maskonite_glass",
			() -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).mapColor(MapColor.TERRACOTTA_CYAN).noOcclusion()));
	public static final RegistryObject<Block> MASKONITE_GLASS_PANE = registerBlock("maskonite_glass_pane",
			() -> new IronBarsBlock(BlockBehaviour.Properties.copy(Blocks.GLASS_PANE).noOcclusion()));
	public static final RegistryObject<Block> SHIVERALIS = registerBlockWithoutBlockItem("shiveralis",
			() -> new ShiveralisPlantBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH).noOcclusion()));
	public static final RegistryObject<Block> POTTED_SHIVERALIS = registerBlockWithoutBlockItem("potted_shiveralis",
			() -> new FlowerPotBlock(ModBlocks.SHIVERALIS.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> RPGIROLLE = registerBlockWithoutBlockItem("rpgirolle",
			() -> new FlowerBlock(MobEffects.SATURATION, 1, BlockBehaviour.Properties.copy(Blocks.DANDELION).mapColor(MapColor.GOLD).noCollission().instabreak().sound(SoundType.FUNGUS)) {

				protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});
	public static final RegistryObject<Block> WILD_FAIRAPIER = registerBlock("wild_fairapier",
			() -> new FlowerBlock(MobEffects.DAMAGE_BOOST, 8, BlockBehaviour.Properties.copy(Blocks.DANDELION).noOcclusion()) {

				protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 10.0D, 15.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});
	public static final RegistryObject<Block> MIMOSSA = registerBlock("mimossa",
			() -> new FlowerBlock(ModEffects.MOSSIOSIS, 16, BlockBehaviour.Properties.copy(Blocks.DANDELION).noOcclusion()) {
				protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 15.0D, 14.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});
	public static final RegistryObject<Block> STAREBLOSSOM = registerBlock("stareblossom",
			() -> new StareblossomBlock(ModEffects.PARANOIA, 16, BlockBehaviour.Properties.copy(Blocks.DANDELION).noOcclusion().offsetType(BlockBehaviour.OffsetType.NONE)));
	public static final RegistryObject<Block> TYPHON = registerBlock("typhon",
			() -> new TyphonBlock(MobEffects.NIGHT_VISION, 3, BlockBehaviour.Properties.copy(Blocks.DANDELION).noOcclusion()) {

				protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 14.0D, 12.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});

	public static final RegistryObject<Block> PARALILY = registerBlockWithoutBlockItem("paralily",
			() -> new ParalilyBlock(BlockBehaviour.Properties.copy(Blocks.LILY_PAD).instabreak().sound(SoundType.LILY_PAD).mapColor(MapColor.COLOR_ORANGE).noOcclusion()));
	public static final RegistryObject<Block> POTTED_MIMOSSA = registerBlockWithoutBlockItem("potted_mimossa",
			() -> new FlowerPotBlock(ModBlocks.MIMOSSA.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> POTTED_STAREBLOSSOM = registerBlockWithoutBlockItem("potted_stareblossom",
			() -> new StareblossomPotBlock(ModBlocks.STAREBLOSSOM.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> POTTED_TYPHON = registerBlockWithoutBlockItem("potted_typhon",
			() -> new FlowerPotBlock(ModBlocks.TYPHON.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> MOSSHROOM = registerBlock("mosshroom",
			() -> new FlowerBlock(ModEffects.MOSSIOSIS, 16, BlockBehaviour.Properties.copy(Blocks.DANDELION).mapColor(MapColor.COLOR_GREEN).noCollission().instabreak().sound(SoundType.MOSS)) {
				protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});
	public static final RegistryObject<Block> POTTED_MOSSHROOM = registerBlockWithoutBlockItem("potted_mosshroom",
			() -> new FlowerPotBlock(ModBlocks.MOSSHROOM.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> CHEESE_CAP = registerBlockWithoutBlockItem("cheese_cap",
			() -> new FlowerBlock(MobEffects.SATURATION, 3, BlockBehaviour.Properties.copy(Blocks.DANDELION).mapColor(MapColor.COLOR_ORANGE).noCollission().instabreak().sound(SoundType.FUNGUS)) {

				protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 13.0D, 12.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});
	public static final RegistryObject<Block> POTTED_CHEESE_CAP = registerBlockWithoutBlockItem("potted_cheese_cap",
			() -> new FlowerPotBlock(ModBlocks.CHEESE_CAP.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> FAIRAPIER_PLANT = registerBlockWithoutBlockItem("fairapier_plant",
			() -> new FairapierPlantBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion()));
	public static final RegistryObject<Block> FAIRAPIER_WILTED_PLANT = registerBlockWithoutBlockItem("fairapier_wilted_plant",
			() -> new FairapierWiltedPlantBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion()));
	public static final RegistryObject<Block> POTTED_RPGIROLLE = registerBlockWithoutBlockItem("potted_rpgirolle",
			() -> new FlowerPotBlock(ModBlocks.RPGIROLLE.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> POTTED_WILD_FAIRAPIER = registerBlockWithoutBlockItem("potted_wild_fairapier",
			() -> new FlowerPotBlock(ModBlocks.WILD_FAIRAPIER.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> PROJECTRUFFLE = registerBlock("projectruffle",
			() -> new FlowerBlock(MobEffects.HARM, 0, BlockBehaviour.Properties.copy(Blocks.DANDELION).mapColor(MapColor.COLOR_BLACK).noOcclusion()) {

				protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});
	public static final RegistryObject<Block> SILICINA = registerBlock("silicina",
			() -> new FlowerBlock(MobEffects.INVISIBILITY, 9, BlockBehaviour.Properties.copy(Blocks.DANDELION).mapColor(MapColor.NONE).noOcclusion()) {

				protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

				@Override
				public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
					Vec3 vec3 = pState.getOffset(pLevel, pPos);
					return SHAPE.move(vec3.x, vec3.y, vec3.z);
				}
			});
	public static final RegistryObject<Block> RAZORLEAF_BUD = registerBlock("razorleaf_bud",
			() -> new RazorleafBudBlock(MobEffects.FIRE_RESISTANCE, 9, BlockBehaviour.Properties.of().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY).mapColor(MapColor.CRIMSON_NYLIUM).noOcclusion()));
	public static final RegistryObject<Block> YOUNG_RAZORLEAF = registerBlock("young_razorleaf",
			() -> new YoungRazorleafBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK).mapColor(MapColor.CRIMSON_NYLIUM).noOcclusion()));
	public static final RegistryObject<Block> TRIPLOVER = registerBlock("triplover", () -> new PinkPetalsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
	public static final RegistryObject<Block> HOLTS_REFLECTION = registerBlock("holts_reflection",
			() -> new HoltsReflectionBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION).noOcclusion()));

	private static ToIntFunction<BlockState> glowingBlockEmission(int pLightValue) {
		return (p_50763_) -> p_50763_.getValue(GLOWING) ? pLightValue : 0;
	}

	public static final RegistryObject<Block> GLOSSOM = registerBlock("glossom",
			() -> new GlossomBlock(MobEffects.GLOWING, 8, BlockBehaviour.Properties.copy(Blocks.DANDELION).lightLevel(glowingBlockEmission(5)).emissiveRendering((blockstate, p_187413_, p_187414_) -> {
				return blockstate.getValue(GLOWING);
			}).noOcclusion()));
	public static final RegistryObject<Block> POTTED_SILICINA = registerBlockWithoutBlockItem("potted_silicina",
			() -> new FlowerPotBlock(ModBlocks.SILICINA.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> POTTED_GLOSSOM = registerBlockWithoutBlockItem("potted_glossom",
			() -> new FlowerPotBlock(ModBlocks.GLOSSOM.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> POTTED_HOLTS_REFLECTION = registerBlockWithoutBlockItem("potted_holts_reflection",
			() -> new FlowerPotBlock(ModBlocks.HOLTS_REFLECTION.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> WIDOWEED = registerBlock("widoweed",
			() -> new WidoweedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().strength(0.2F).sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)));
	public static final RegistryObject<Block> SPIKY_IVY = registerBlock("spiky_ivy",
			() -> new SpikyIvyBlock(BlockBehaviour.Properties.of().randomTicks().strength(0.4F).sound(SoundType.WART_BLOCK)));
	public static final RegistryObject<Block> POTTED_SPIKY_IVY = registerBlockWithoutBlockItem("potted_spiky_ivy",
			() -> new FlowerPotBlock(ModBlocks.SPIKY_IVY.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));
	public static final RegistryObject<Block> POTTED_PROJECTRUFFLE = registerBlockWithoutBlockItem("potted_projectruffle",
			() -> new FlowerPotBlock(ModBlocks.PROJECTRUFFLE.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).noOcclusion()));


	public static final RegistryObject<Block> LIVING_WOOD_LOG = registerBlock("living_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_STEM).mapColor(MapColor.GLOW_LICHEN)));
	public static final RegistryObject<Block> LIVING_WOOD_WOOD = registerBlock("living_wood_wood",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_HYPHAE).mapColor(MapColor.TERRACOTTA_GRAY)));
	public static final RegistryObject<Block> STRIPPED_LIVING_WOOD_LOG = registerBlock("stripped_living_wood_log",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_WARPED_STEM).mapColor(MapColor.GLOW_LICHEN)));
	public static final RegistryObject<Block> STRIPPED_LIVING_WOOD_WOOD = registerBlock("stripped_living_wood_wood",
			() -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_WARPED_HYPHAE).mapColor(MapColor.GLOW_LICHEN)));

	public static final RegistryObject<Block> RIE_LOG = registerBlock("rie_log",
			() -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).mapColor(MapColor.GLOW_LICHEN)));
	public static final RegistryObject<Block> RIE_WOOD = registerBlock("rie_wood",
			() -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).mapColor(MapColor.TERRACOTTA_GRAY)));
	public static final RegistryObject<Block> STRIPPED_RIE_LOG = registerBlock("stripped_rie_log",
			() -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.GLOW_LICHEN)));
	public static final RegistryObject<Block> STRIPPED_RIE_WOOD = registerBlock("stripped_rie_wood",
			() -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD).mapColor(MapColor.GLOW_LICHEN)));
	public static final RegistryObject<Block> RIE_HOLLOW = registerBlock("rie_hollow",
			() -> new TreeHollowBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(2.0F).mapColor(MapColor.GLOW_LICHEN)));


	public static final RegistryObject<Block> RIE_PLANKS = registerBlock("rie_planks",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).mapColor(MapColor.GLOW_LICHEN)) {
				@Override
				public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return true;
				}

				@Override
				public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 20;
				}

				@Override
				public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 5;
				}
			});
	public static final RegistryObject<Block> RIE_STAIRS = registerBlock("rie_stairs",
			() -> new StairBlock(() -> ModBlocks.RIE_PLANKS.get().defaultBlockState(),
					BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS).mapColor(MapColor.GLOW_LICHEN)) {
				@Override
				public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return true;
				}

				@Override
				public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 20;
				}

				@Override
				public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 5;
				}
			});
	public static final RegistryObject<Block> RIE_SLAB = registerBlock("rie_slab",
			() -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB).mapColor(MapColor.GLOW_LICHEN)) {
				@Override
				public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return true;
				}

				@Override
				public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 20;
				}

				@Override
				public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 5;
				}
			});
	public static final RegistryObject<Block> RIE_FENCE = registerFuelBlock("rie_fence",
			() -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE).mapColor(MapColor.GLOW_LICHEN)) {
				@Override
				public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return true;
				}

				@Override
				public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 20;
				}

				@Override
				public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 5;
				}
			}, 300);
	public static final RegistryObject<Block> RIE_FENCE_GATE = registerFuelBlock("rie_fence_gate",
			() -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE).mapColor(MapColor.GLOW_LICHEN), ModWoodTypes.RIE_WOOD_TYPE) {
				@Override
				public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return true;
				}

				@Override
				public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 20;
				}

				@Override
				public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 5;
				}
			}, 300);

	public static final RegistryObject<Block> RIE_BUTTON = registerBlock("rie_button",
			() -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON), ModWoodTypes.RIE, 30, true));
	public static final RegistryObject<Block> RIE_PRESSURE_PLATE = registerBlock("rie_pressure_plate",
			() -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE).mapColor(MapColor.GLOW_LICHEN), ModWoodTypes.RIE));
	public static final RegistryObject<Block> RIE_DOOR = registerBlock("rie_door",
			() -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR).mapColor(MapColor.GLOW_LICHEN), ModWoodTypes.RIE));
	public static final RegistryObject<Block> RIE_TRAPDOOR = registerBlock("rie_trapdoor",
			() -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR).mapColor(MapColor.GLOW_LICHEN), ModWoodTypes.RIE));


	public static final RegistryObject<Block> RIE_LEAVES = registerBlock("rie_leaves",
			() -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)) {
				@Override
				public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return true;
				}

				@Override
				public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 60;
				}

				@Override
				public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
					return 30;
				}
			});
	public static final RegistryObject<Block> RIE_SAPLING = registerBlock("rie_sapling",
			() -> new SaplingBlock(new RieTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
	public static final RegistryObject<Block> POTTED_RIE_SAPLING = registerBlockWithoutBlockItem("potted_rie_sapling",
			() -> new FlowerPotBlock(ModBlocks.RIE_SAPLING.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_OAK_SAPLING).noOcclusion()));
	public static final RegistryObject<Block> RIE_WALL_SIGN = registerBlockWithoutBlockItem("rie_wall_sign",
			() -> new ModWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN), ModWoodTypes.RIE_WOOD_TYPE));
	public static final RegistryObject<Block> RIE_SIGN = registerBlockWithoutBlockItem("rie_sign",
			() -> new ModStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN).mapColor(MapColor.GLOW_LICHEN), ModWoodTypes.RIE_WOOD_TYPE));
	public static final RegistryObject<CeilingHangingSignBlock> RIE_HANGING_SIGN = BLOCKS.register("rie_hanging_sign", () -> new ModCeilingHangingSignBlock(BlockBehaviour.Properties.copy(RIE_PLANKS.get()).noCollission().strength(1.0F), ModWoodTypes.RIE_WOOD_TYPE));
	public static final RegistryObject<WallHangingSignBlock> RIE_WALL_HANGING_SIGN = BLOCKS.register("rie_wall_hanging_sign", () -> new ModWallHangingSignBlock(BlockBehaviour.Properties.copy(RIE_PLANKS.get()).noCollission().strength(1.0F), ModWoodTypes.RIE_WOOD_TYPE));

	public static final RegistryObject<LiquidBlock> ARBOR_FUEL_BLOCK = BLOCKS.register("arbor_fuel_block", () -> new FuelLiquidBlock(ModFluids.SOURCE_ARBOR_FUEL, BlockBehaviour.Properties.copy(Blocks.WATER).strength(100.0F).noLootTable().replaceable().liquid().pushReaction(PushReaction.DESTROY).noOcclusion()) {
		@Override
		public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
			return true;
		}

		@Override
		public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
			return 300;
		}

		@Override
		public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
			return 50;
		}
	});

	public static final RegistryObject<Block> MINTAL_BLOCK = registerBlock("mintal_block",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).mapColor(MapColor.WARPED_STEM).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> WINGOLD_BLOCK = registerBlock("wingold_block",
			() -> new Block(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GOLD).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
	public static final RegistryObject<Block> ARBOR_FUEL_CAULDRON = registerBlockWithoutBlockItem("arbor_fuel_cauldron", () -> new ArborFuelCauldron(BlockBehaviour.Properties.copy(Blocks.CAULDRON), ModCauldronInteraction.ARBOR_FUEL));

	public static final RegistryObject<Block> MOSQUITOS = registerBlockWithoutBlockItem("mosquitos", () -> new MosquitosBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).replaceable().noCollission().strength(0.2F).sound(SoundType.FROGSPAWN).pushReaction(PushReaction.DESTROY)));

	public static <T extends Block> RegistryObject<T> registerBlockWithoutBlockItem(String name, Supplier<T> block) {
		return BLOCKS.register(name, block);
	}

	public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
		RegistryObject<T> toReturn = BLOCKS.register(name, block);
		registerBlockItem(name, toReturn);
		return toReturn;
	}

	public static <T extends Block> RegistryObject<T> registerBlockWithTooltip(String name, Supplier<T> block) {
		RegistryObject<T> toReturn = BLOCKS.register(name, block);
		registerBlockItemWithTooltip(name, toReturn);
		return toReturn;
	}

	public static <T extends Block> RegistryObject<T> registerFuelBlock(String name, Supplier<T> block, int burntime) {
		RegistryObject<T> toReturn = BLOCKS.register(name, block);
		ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()) {
			@Override
			public int getBurnTime(ItemStack itemstack, @Nullable RecipeType<?> recipeType) {
				return burntime;
			}
		});
		return toReturn;
	}

	public static <T extends Block> RegistryObject<T> registerFuelBlockWithTooltip(String name, Supplier<T> block, int burntime) {
		RegistryObject<T> toReturn = BLOCKS.register(name, block);
		ModItems.ITEMS.register(name, () -> new TooltipBlockItem(toReturn.get(), new Item.Properties()) {
			@Override
			public int getBurnTime(ItemStack itemstack, @Nullable RecipeType<?> recipeType) {
				return burntime;
			}
		});
		return toReturn;
	}

	public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
		return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
	}

	public static <T extends Block> RegistryObject<Item> registerBlockItemWithTooltip(String name, RegistryObject<T> block) {
		return ModItems.ITEMS.register(name, () -> new TooltipBlockItem(block.get(), new Item.Properties()));
	}

	public static void register(IEventBus eventbus) {
		BLOCKS.register(eventbus);
	}

}
