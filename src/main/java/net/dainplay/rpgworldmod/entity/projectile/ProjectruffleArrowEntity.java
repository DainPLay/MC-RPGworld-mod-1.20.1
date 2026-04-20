package net.dainplay.rpgworldmod.entity.projectile;

import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProjectruffleArrowEntity extends AbstractArrow {
	@Override
	public double getBaseDamage() {
		return 0.0F;
	}

	@Override
	protected float getWaterInertia() {
		return 1.0F;
	}

	public ProjectruffleArrowEntity(EntityType<ProjectruffleArrowEntity> entityType, Level world) {
		super(entityType, world);
	}

	public ProjectruffleArrowEntity(EntityType<ProjectruffleArrowEntity> entityType, double x, double y, double z, Level world) {
		super(entityType, x, y, z, world);
	}


	public ProjectruffleArrowEntity(EntityType<ProjectruffleArrowEntity> entityType, LivingEntity shooter, Level world) {
		super(entityType, shooter, world);
	}


	@Override
	public ItemStack getPickupItem() {
		return new ItemStack(ModItems.PROJECTRUFFLE_ITEM.get());
	}

	protected void doPostHurtEffects(LivingEntity entity) {
		super.doPostHurtEffects(entity);
		entity.setArrowCount(entity.getArrowCount() - 1);
	}

}