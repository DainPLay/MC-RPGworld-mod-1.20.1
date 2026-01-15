package net.dainplay.rpgworldmod.data.tags;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.damage.ModDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class DamageTypeTagGenerator extends TagsProvider<DamageType> {

	public static final TagKey<DamageType> IS_SOUND = create("is_sound");
	public DamageTypeTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
		super(output, Registries.DAMAGE_TYPE, future, RPGworldMod.MOD_ID, helper);
	}

	protected void addTags(HolderLookup.Provider provider) {
		this.tag(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.MOSSIOSIS, ModDamageTypes.SCREAM, ModDamageTypes.DING, ModDamageTypes.MOSQUITOS);
		this.tag(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(ModDamageTypes.MOSSIOSIS, ModDamageTypes.MOSQUITOS);
		this.tag(DamageTypeTags.BYPASSES_COOLDOWN).add(ModDamageTypes.MOSSIOSIS, ModDamageTypes.SWALLOW,ModDamageTypes.ENT_SNAP);
		this.tag(DamageTypeTags.IS_FIRE).add(ModDamageTypes.SWALLOW);
		this.tag(DamageTypeTags.BYPASSES_INVULNERABILITY).add(ModDamageTypes.SWALLOW);
		this.tag(DamageTypeTags.BYPASSES_EFFECTS).add(ModDamageTypes.MOSSIOSIS);
		this.tag(DamageTypeTags.BYPASSES_RESISTANCE).add(ModDamageTypes.MOSSIOSIS);
		this.tag(DamageTypeTags.BYPASSES_SHIELD).add(ModDamageTypes.MOSSIOSIS, ModDamageTypes.SCREAM, ModDamageTypes.DING, ModDamageTypes.MOSQUITOS, ModDamageTypes.SWALLOW,ModDamageTypes.ENT_ROOTS);
		this.tag(DamageTypeTags.DAMAGES_HELMET).add(ModDamageTypes.SCREAM, ModDamageTypes.DING);
		this.tag(DamageTypeTags.IS_PROJECTILE).add(ModDamageTypes.FAIRAPIER_SEED,ModDamageTypes.BURR_SPIKE,ModDamageTypes.ENT_RIE_FRUIT,ModDamageTypes.ENT_ROOTS);
		this.tag(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).add(ModDamageTypes.SCREAM, ModDamageTypes.DING, ModDamageTypes.MOSQUITOS,ModDamageTypes.FAIRAPIER_SEED,ModDamageTypes.BURR_SPIKE,ModDamageTypes.ENT_RIE_FRUIT);
		this.tag(IS_SOUND).add(ModDamageTypes.SCREAM, ModDamageTypes.DING, DamageTypes.SONIC_BOOM);
	}

	private static TagKey<DamageType> create(String name) {
		return TagKey.create(Registries.DAMAGE_TYPE, RPGworldMod.prefix(name));
	}
}
