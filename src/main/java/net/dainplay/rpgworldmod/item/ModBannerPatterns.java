package net.dainplay.rpgworldmod.item;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModBannerPatterns {
	public static final DeferredRegister<BannerPattern> BANNER_PATTERNS = DeferredRegister.create(Registries.BANNER_PATTERN, RPGworldMod.MOD_ID);

	public static final RegistryObject<BannerPattern> RIE_WEALD = BANNER_PATTERNS.register("rie_weald", () -> new BannerPattern("rie"));
}