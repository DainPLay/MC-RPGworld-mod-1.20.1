package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.entity.projectile.ProjectruffleArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProjectruffleArrowItem extends ArrowItem {
    public ProjectruffleArrowItem(Properties props) {
        super(props);
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack ammoStack, LivingEntity shooter) {
        return new ProjectruffleArrowEntity(ModEntities.PROJECTRUFFLE_ARROW.get(), shooter, world);
    }
}