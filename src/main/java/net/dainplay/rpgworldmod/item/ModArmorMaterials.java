package net.dainplay.rpgworldmod.item;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
	LIVING_WOOD("living_wood", 15, new int[]{2,5,4,1}, 25,
			RPGSounds.ARMOR_EQUIP_LIVING_WOOD.get(), 0F, 0F, () -> Ingredient.of(ModTags.Items.LIVING_WOOD_LOGS));

	private final String name;
	private final int durabilityMultiplier;
	private final int[] protectionAmounts;
	private final int enhantmentValue;
	private final SoundEvent equipSound;
	private final float toughness;
	private final float knockbackResistance;
	private final Supplier<Ingredient> repairIngredient;

	private static final int[] BASE_DURABILITY = new int[] {11, 16, 16, 13};

	ModArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enhantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.protectionAmounts = protectionAmounts;
		this.enhantmentValue = enhantmentValue;
		this.equipSound = equipSound;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
		this.repairIngredient = repairIngredient;
	}


	@Override
	public int getDurabilityForType(ArmorItem.Type pType) {
		return BASE_DURABILITY[pType.ordinal()] * this.durabilityMultiplier;
	}

	@Override
	public int getDefenseForType(ArmorItem.Type pType) {
		return this.protectionAmounts[pType.ordinal()];
	}

	@Override
	public int getEnchantmentValue() {
		return this.enhantmentValue;
	}

	@Override
	public SoundEvent getEquipSound() {
		return this.equipSound;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairIngredient.get();
	}

	@Override
	public String getName() {
		return RPGworldMod.MOD_ID + ":" + this.name;
	}

	@Override
	public float getToughness() {
		return this.toughness;
	}

	@Override
	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}
}
