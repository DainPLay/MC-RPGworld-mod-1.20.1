package net.dainplay.rpgworldmod.util;

import com.mojang.serialization.Dynamic;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.item.custom.GasbassItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class FoodSmokeParticlesHandler {

	@SubscribeEvent
	public static void onLivingEntityUseItem(LivingEntityUseItemEvent event) {
		ItemStack itemStack = event.getItem();
		LivingEntity entity = event.getEntity();

		if (itemStack.getItem() instanceof GasbassItem) {
			emitSmokeParticles(entity);
			if (itemStack.hasTag() && itemStack.getTag().contains("rpgworldmod.return_pos_x")) {
				double teleportPosX;
				double teleportPosY;
				double teleportPosZ;
				ResourceKey<Level> teleportDimension;
				teleportPosX = itemStack.getTag().getDouble("rpgworldmod.return_pos_x");
				teleportPosY = itemStack.getTag().getDouble("rpgworldmod.return_pos_y");
				teleportPosZ = itemStack.getTag().getDouble("rpgworldmod.return_pos_z");
				teleportDimension = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE,
								itemStack.getTag().get("rpgworldmod.return_pos_dimension")))
						.resultOrPartial(RPGworldMod.LOGGER::error)
						.orElse(entity.getCommandSenderWorld().dimension());
				if(!entity.level().isClientSide) {
					if (entity.getServer() != null && entity.getServer().getLevel(teleportDimension) != null)
						emitSmokeParticles(teleportPosX, teleportPosY, teleportPosZ, entity.getRandom(), entity.getServer().getLevel(teleportDimension));
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event) {
		ItemStack itemStack = event.getItem();
		LivingEntity entity = event.getEntity();

		if (itemStack.getItem() instanceof GasbassItem) {
			emitSmokeParticles(entity);
			if (itemStack.hasTag() && itemStack.getTag().contains("rpgworldmod.return_pos_x")) {
				double teleportPosX;
				double teleportPosY;
				double teleportPosZ;
				ResourceKey<Level> teleportDimension;
				teleportPosX = itemStack.getTag().getDouble("rpgworldmod.return_pos_x");
				teleportPosY = itemStack.getTag().getDouble("rpgworldmod.return_pos_y");
				teleportPosZ = itemStack.getTag().getDouble("rpgworldmod.return_pos_z");
				teleportDimension = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE,
								itemStack.getTag().get("rpgworldmod.return_pos_dimension")))
						.resultOrPartial(RPGworldMod.LOGGER::error)
						.orElse(entity.getCommandSenderWorld().dimension());
				if(!entity.level().isClientSide) {
					if (entity.getServer() != null && entity.getServer().getLevel(teleportDimension) != null)
						emitSmokeParticles(teleportPosX, teleportPosY, teleportPosZ, entity.getRandom(), entity.getServer().getLevel(teleportDimension));
				}
			}
		}
	}

	private static void emitSmokeParticles(LivingEntity entity) {
		double x = entity.getX();
		double y = entity.getY() + entity.getEyeHeight() / 2.0;
		double z = entity.getZ();

		for (int i = 0; i < 10; i++) {
			double offsetX = (entity.getRandom().nextDouble() - 0.5) * 0.8;
			double offsetY = (entity.getRandom().nextDouble() - 0.5) * 0.4;
			double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 0.8;

			entity.level().addParticle(
					ParticleTypes.LARGE_SMOKE,
					x + offsetX,
					y + offsetY,
					z + offsetZ,
					0.0, 0.02, 0.0
			);
		}
	}

	private static void emitSmokeParticles(double x, double y, double z, RandomSource random, ServerLevel level) {

		double offsetX = (random.nextDouble() - 0.5) * 0.8;
		double offsetY = (random.nextDouble() - 0.5) * 0.4;
		double offsetZ = (random.nextDouble() - 0.5) * 0.8;

		level.sendParticles(
				ParticleTypes.LARGE_SMOKE,
				x,
				y,
				z, 10,
				offsetX, offsetY, offsetZ, 0
		);
	}
}
