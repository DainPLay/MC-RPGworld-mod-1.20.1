package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.entity.custom.ModBoat;
import net.dainplay.rpgworldmod.entity.custom.ModChestBoat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ModBoatDispenseItemBehavior extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final ModBoat.Type type;
    private final boolean isChestBoat;

    public ModBoatDispenseItemBehavior(ModBoat.Type type) {
        this(type, false);
    }

    public ModBoatDispenseItemBehavior(ModBoat.Type type, boolean isChestBoat) {
        this.type = type;
        this.isChestBoat = isChestBoat;
    }

    @Override
    public ItemStack execute(BlockSource source, ItemStack stack) {
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
        Level level = source.getLevel();

        // Calculate position like vanilla does
        double d0 = 0.5625D + (double) EntityType.BOAT.getWidth() / 2.0D; // Use your custom entity type's width if different
        double x = source.x() + (double) direction.getStepX() * d0;
        double y = source.y() + (double) ((float) direction.getStepY() * 1.125F);
        double z = source.z() + (double) direction.getStepZ() * d0;

        BlockPos relativePos = source.getPos().relative(direction);

        // Create the appropriate boat
        ModBoat boat;
        if (isChestBoat) {
            boat = new ModChestBoat(level, x, y, z);
            (boat).setVariant(this.type); // or setModBoatType, depending on your method name
        } else {
            boat = new ModBoat(level, x, y, z);
            (boat).setVariant(this.type);
        }
        boat.setVariant(this.type); // This line might be redundant if you already set above, but keep for clarity
        boat.setYRot(direction.toYRot());

        // Determine vertical offset based on fluid
        double yOffset;
        if (boat.canBoatInFluid(level.getFluidState(relativePos))) {
            yOffset = 1.0D;
        } else {
            if (!level.getBlockState(relativePos).isAir() || !boat.canBoatInFluid(level.getFluidState(relativePos.below()))) {
                // Not placeable – fall back to dropping the item
                return this.defaultDispenseItemBehavior.dispense(source, stack);
            }
            yOffset = 0.0D;
        }

        // Set final position and spawn
        boat.setPos(x, y + yOffset, z);
        level.addFreshEntity(boat);
        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        // Play the dispenser "shoot" sound (same as vanilla boats)
        source.getLevel().levelEvent(1000, source.getPos(), 0);
    }
}