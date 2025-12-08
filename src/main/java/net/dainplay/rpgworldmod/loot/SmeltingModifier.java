package net.dainplay.rpgworldmod.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SmeltingModifier extends LootModifier {
	public static final Codec<SmeltingModifier> CODEC = RecordCodecBuilder.create(inst -> LootModifier.codecStart(inst).apply(inst, SmeltingModifier::new));

	public SmeltingModifier(LootItemCondition[] conditions) {
		super(conditions);
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();

		ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
		int fortuneLevel = 0;
		Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);

		if (tool != null) {
			fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
		}

		float chance = 0.1F + (fortuneLevel * 0.3F);
		chance = Math.min(chance, 1.0f);

		for (ItemStack stack : generatedLoot) {
			boolean shouldSmelt = shouldSmeltItem(stack, context, chance);

			if (shouldSmelt) {
				ItemStack smelted = context.getLevel().getRecipeManager()
						.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), context.getLevel())
						.map(recipe -> recipe.getResultItem(context.getLevel().registryAccess()))
						.filter(itemStack -> !itemStack.isEmpty())
						.map(itemStack -> ItemHandlerHelper.copyStackWithSize(itemStack, stack.getCount() * itemStack.getCount()))
						.orElse(stack);
				newLoot.add(smelted);
				if(!context.getLevel().isClientSide && origin != null) {
					BlockPos blockPos = new BlockPos(
							(int) Math.floor(origin.x),
							(int) Math.floor(origin.y),
							(int) Math.floor(origin.z)
					);
					if (!smelted.isEmpty() && smelted != stack) {
						float totalExperience = context.getLevel().getRecipeManager()
								.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), context.getLevel())
								.map(recipe -> recipe.getExperience())
								.orElse(0f);

						if (totalExperience > 0f) {
							spawnExperience(context, blockPos, (int)(totalExperience*10));
						}
						context.getLevel().sendParticles(ParticleTypes.FLAME, blockPos.getX()+0.5f, blockPos.getY()+0.5f, blockPos.getZ()+0.5f, 20, context.getLevel().getRandom().nextFloat()/5, context.getLevel().getRandom().nextFloat()/5, context.getLevel().getRandom().nextFloat()/5, 0.01f);
					}
				}
			} else {
				newLoot.add(stack);
			}
		}

		return newLoot;
	}

	private void spawnExperience(LootContext context, BlockPos blockPos, int experienceAmount) {
		if (context.getLevel().isClientSide() || blockPos == null || experienceAmount <= 0) {
			return;
		}

		ServerLevel serverLevel = context.getLevel();

		ExperienceOrb experienceOrb = new ExperienceOrb(
				serverLevel,
				blockPos.getX() + 0.5,
				blockPos.getY() + 0.5,
				blockPos.getZ() + 0.5,
				experienceAmount
		);

		serverLevel.addFreshEntity(experienceOrb);
	}


	private boolean shouldSmeltItem(ItemStack stack, LootContext context, float chance) {
		if (context.getRandom().nextFloat() < chance) {
			return true;
		}
		return false;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return SmeltingModifier.CODEC;
	}
}
