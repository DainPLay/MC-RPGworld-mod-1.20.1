package net.dainplay.rpgworldmod.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.biome.BiomeRegistry;
import net.dainplay.rpgworldmod.biome.RPGworldBiomeDecorator;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.dainplay.rpgworldmod.RPGworldMod.LOGGER;
import static net.dainplay.rpgworldmod.RPGworldMod.MOD_ID;
import static net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures.RIE_WEALD_MUSIC;
import static net.dainplay.rpgworldmod.world.feature.ModConfiguredFeatures.RIE_WEALD_MUSIC_FOG;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class FogEventHandler {

    private static final float[] rieFogColors = new float[3];
    private static float rieFogColor = 0F;
    private static float rieFog = 1F;

    @SubscribeEvent
    public static void fogColors(ViewportEvent.ComputeFogColor event) {
        //LOGGER.info("Cooldown:" + Minecraft.getInstance().player.getCooldowns().getCooldownPercent(ModItems.MINTAL_TRIANGLE.get(), 0F));

        //LOGGER.info("blocking: " + ItemProperties.getProperty(Minecraft.getInstance().player.getMainHandItem().getItem(),new ResourceLocation("blocking")));

        boolean flag = isInRieWeald();
        if ((flag || rieFogColor > 0F) && event.getCamera().getFluidInCamera() == FogType.NONE && !(event.getCamera().getBlockAtCamera().getBlock() instanceof LiquidBlock)) {
            final float[] realColors = {event.getRed(), event.getGreen(), event.getBlue()};
            final float[] lerpColors = {255F / 255F, 255F / 255F, 255F / 255F};
            for (int i = 0; i < 3; i++) {
                final float real = realColors[i];
                final float rie_fog = lerpColors[i];
                final boolean inverse = real > rie_fog;
                rieFogColors[i] = real == rie_fog ? rie_fog : Mth.clampedLerp(inverse ? rie_fog : real, inverse ? real : rie_fog, rieFogColor);
            }
            float shift = (float) (0.01F * event.getPartialTick());
            if (flag)
                rieFogColor += shift;
            else
                rieFogColor -= shift;
            rieFogColor = Mth.clamp(rieFogColor, 0F, 1F);
            event.setRed(rieFogColors[0]);
            event.setGreen(rieFogColors[1]);
            event.setBlue(rieFogColors[2]);
        }
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog event) {
        boolean flag = isInRieWeald();
        if ((flag || rieFog < 1F) && event.getCamera().getFluidInCamera() == FogType.NONE && !(event.getCamera().getBlockAtCamera().getBlock() instanceof LiquidBlock)) {
            float f = 30F;
            f = f >= event.getFarPlaneDistance() ? event.getFarPlaneDistance() : Mth.clampedLerp(f, event.getFarPlaneDistance(), rieFog);
            float shift = (float) (0.001F * event.getPartialTick());
            if (flag)
                rieFog -= shift;
            else
                rieFog += shift;
            rieFog = Mth.clamp(rieFog, 0F, 1F);

            if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
                RenderSystem.setShaderFogStart(0.0F);
                RenderSystem.setShaderFogEnd(f);
            } else {
                RenderSystem.setShaderFogStart(0.0F);
                RenderSystem.setShaderFogEnd(f);
            }
        }
    }

    public static boolean isInRieWeald() {
        return Minecraft.getInstance().level != null && Minecraft.getInstance().player != null &&
                Minecraft.getInstance().level.getBiome(Minecraft.getInstance().player.blockPosition()).is(BiomeRegistry.RIE_WEALD)
                && !Minecraft.getInstance().player.hasEffect(MobEffects.BLINDNESS)
                && (Minecraft.getInstance().player.level().isRaining() || Minecraft.getInstance().player.level().isThundering());
    }
}