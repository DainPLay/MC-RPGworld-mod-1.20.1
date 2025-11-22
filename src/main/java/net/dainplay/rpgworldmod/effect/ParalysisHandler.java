package net.dainplay.rpgworldmod.effect;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.sounds.ModSounds;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class ParalysisHandler {

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (hasParalysisEffect(player)) {
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                event.setCanceled(true); // Cancel the mouse scroll event
            }
        }
    }
    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && hasParalysisEffect(player) && event.isPickBlock()) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
           event.setCanceled(true);
        }
        }
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && hasParalysisEffect(player)) {
            if (Minecraft.getInstance().options.keyHotbarSlots[0].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[0].setDown(false);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
        }
            if (Minecraft.getInstance().options.keyHotbarSlots[1].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[1].setDown(false);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
    }
            if (Minecraft.getInstance().options.keyHotbarSlots[2].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[2].setDown(false);
            }
            if (Minecraft.getInstance().options.keyHotbarSlots[3].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[3].setDown(false);
            }
            if (Minecraft.getInstance().options.keyHotbarSlots[4].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[4].setDown(false);
            }
            if (Minecraft.getInstance().options.keyHotbarSlots[5].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[5].setDown(false);
            }
            if (Minecraft.getInstance().options.keyHotbarSlots[6].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[6].setDown(false);
            }
            if (Minecraft.getInstance().options.keyHotbarSlots[7].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[7].setDown(false);
            }
            if (Minecraft.getInstance().options.keyHotbarSlots[8].consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyHotbarSlots[8].setDown(false);
            }
            if (Minecraft.getInstance().options.keySwapOffhand.consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keySwapOffhand.setDown(false);
            }
            if (Minecraft.getInstance().options.keyDrop.consumeClick()){
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                Minecraft.getInstance().options.keyDrop.setDown(false);
            }
        }
    }
    @SubscribeEvent
    public static void onMouseClickInInventory(InputEvent.MouseButton event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (hasParalysisEffect(player) && Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
                if(event.getAction()==GLFW.GLFW_PRESS) Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                event.setCanceled(true); // Cancel the mouse scroll event
            }
        }
    }
    @SubscribeEvent
    public static void onInputInInventory(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (hasParalysisEffect(player) && (Minecraft.getInstance().screen instanceof ContainerScreen || Minecraft.getInstance().screen instanceof InventoryScreen)
                    && (Minecraft.getInstance().options.keyDrop.getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyPickItem.getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keySwapOffhand.getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[0].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[1].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[2].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[3].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[4].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[5].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[6].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[7].getKey().getValue() == event.getKey()
                    || Minecraft.getInstance().options.keyHotbarSlots[8].getKey().getValue() == event.getKey())) {
                
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(RPGSounds.PARALISED.get(), 1.0F, 0.1F));
                //event.isCanceled();
            }
        }
    }
    private static boolean hasParalysisEffect(Player player) {
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)) return false;
        for (MobEffectInstance effect : player.getActiveEffects()) {
            MobEffect mobEffect = effect.getEffect();
            if (mobEffect == ModEffects.PARALYSIS.get()) {
                return true;
            }
        }
        return false;
    }

}