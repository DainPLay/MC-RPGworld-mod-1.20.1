package net.dainplay.rpgworldmod.block.entity;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModWoodTypes {
    public static BlockSetType RIE = new BlockSetType(RPGworldMod.prefix("rie").toString());
    public static final WoodType RIE_WOOD_TYPE = WoodType.register(new WoodType(RPGworldMod.prefix("rie").toString(), RIE));
}
