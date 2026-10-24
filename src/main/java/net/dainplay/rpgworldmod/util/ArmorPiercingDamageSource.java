package net.dainplay.rpgworldmod.util;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ArmorPiercingDamageSource extends DamageSource {

    public ArmorPiercingDamageSource(DamageSource original) {
        super(original.typeHolder(),
              original.getDirectEntity(),
              original.getEntity(),
              original.getSourcePosition());
    }

    public ArmorPiercingDamageSource(DamageSource original, Entity direct, Entity causing, @Nullable Vec3 pos) {
        super(original.typeHolder(), direct, causing, pos);
    }

    @Override
    public boolean is(TagKey<DamageType> key) {
        if (key == DamageTypeTags.BYPASSES_ARMOR
         || key == DamageTypeTags.BYPASSES_EFFECTS
         || key == DamageTypeTags.BYPASSES_ENCHANTMENTS
         || key == DamageTypeTags.BYPASSES_RESISTANCE
         || key == DamageTypeTags.BYPASSES_COOLDOWN) {
            return true;
        }
        return super.is(key);
    }
}