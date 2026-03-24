package net.dainplay.rpgworldmod.mixin;

import net.dainplay.rpgworldmod.item.custom.StaffItem;
import net.dainplay.rpgworldmod.network.C2STriggerChestStaffsPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.util.ITriggerChestStaffs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenuClick implements ITriggerChestStaffs {

    @Unique
    private boolean rpgworldmod$sentTrigger = false;

    @Override
    public boolean rpgworldmod$hasSentTrigger() {
        return this.rpgworldmod$sentTrigger;
    }

    @Override
    public void rpgworldmod$setSentTrigger(boolean sent) {
        this.rpgworldmod$sentTrigger = sent;
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void onClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (player != Minecraft.getInstance().player) return;

        if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE || clickType == ClickType.SWAP) {
            Minecraft.getInstance().submitAsync(this::checkAndSendTriggerDelayed);
        }
    }

    @Unique
    private void checkAndSendTriggerDelayed() {
        new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
            Minecraft.getInstance().execute(this::checkAndSendTrigger);
        }).start();
    }

    @Unique
    private void checkAndSendTrigger() {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (this.rpgworldmod$hasSentTrigger()) return;

        Set<String> uniqueKeys = new HashSet<>();
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory) continue;
            if (slot instanceof CreativeModeInventoryScreen.CustomCreativeSlot) continue;

            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof StaffItem staff) {
                StaffItem.GemType gem = StaffItem.getGemType(stack);
                String key = BuiltInRegistries.ITEM.getKey(staff).toString() + "_" + gem.getName();
                uniqueKeys.add(key);
                if (uniqueKeys.size() >= 27) {
                    ModMessages.sendToServer(new C2STriggerChestStaffsPacket());
                    this.rpgworldmod$setSentTrigger(true);
                    break;
                }
            }
        }

        if (uniqueKeys.size() < 27 && this.rpgworldmod$hasSentTrigger()) {
            this.rpgworldmod$setSentTrigger(false);
        }
    }
}