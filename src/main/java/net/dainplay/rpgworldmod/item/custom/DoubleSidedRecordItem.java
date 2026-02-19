package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class DoubleSidedRecordItem extends RecordItem implements RPGtooltip {

	public DoubleSidedRecordItem(int analogOutput, SoundEvent sound, Properties properties, int lengthInSeconds) {
		super(analogOutput, sound, properties, lengthInSeconds);
	}

	// Методы для работы с NBT
	private static final String TAG_FLIP_STAGE = "flip_stage";
	private static final String TAG_FLIP_REMAINING = "flip_remaining";

	public static int getFlipStage(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null ? tag.getInt(TAG_FLIP_STAGE) : 0;
	}

	private static int getFlipRemaining(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null ? tag.getInt(TAG_FLIP_REMAINING) : 0;
	}

	private static void setFlip(ItemStack stack, int stage, int remaining) {
		CompoundTag tag = stack.getOrCreateTag();
		tag.putInt(TAG_FLIP_STAGE, stage);
		tag.putInt(TAG_FLIP_REMAINING, remaining);
	}

	public static void removeFlip(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null) {
			tag.remove(TAG_FLIP_STAGE);
			tag.remove(TAG_FLIP_REMAINING);
			if (tag.isEmpty()) {
				stack.setTag(null);
			}
		}
	}

	public Item getOtherSide(Item item) {
		if (item == ModItems.MUSIC_DISC_RAIN_A_SIDE.get()) return ModItems.MUSIC_DISC_RAIN_B_SIDE.get();
		if (item == ModItems.MUSIC_DISC_RAIN_B_SIDE.get()) return ModItems.MUSIC_DISC_RAIN_A_SIDE.get();
		return item;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (level.isClientSide) return;

		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(TAG_FLIP_STAGE)) return;

		int stage = tag.getInt(TAG_FLIP_STAGE);
		int remaining = tag.getInt(TAG_FLIP_REMAINING) - 1;

		if (remaining <= 0) {
			if (stage < 7) {
				// Переход на следующую стадию
				stage++;
				remaining = 1;
			} else if (stage == 7) {
				// Замена на другую сторону
				if (entity instanceof Player player) {
					// Создаём новый стек с предметом otherSide
					ItemStack newStack = new ItemStack(getOtherSide(stack.getItem()), stack.getCount());

					// Копируем все NBT, кроме тегов flip
					CompoundTag oldTag = stack.getTag();
					if (oldTag != null) {
						CompoundTag newTag = oldTag.copy();
						newTag.remove(TAG_FLIP_STAGE);
						newTag.remove(TAG_FLIP_REMAINING);
						if (!newTag.isEmpty()) {
							newStack.setTag(newTag);
						}
					}

					// Заменяем предмет в инвентаре
					player.getInventory().setItem(slotId, newStack);
				} else {
					// Если владелец не игрок (например, моб) – просто удаляем flip-теги
					removeFlip(stack);
				}
				return; // не обновляем теги, так как предмет либо заменён, либо теги удалены
			}
		}

		// Сохраняем обновлённые значения
		tag.putInt(TAG_FLIP_STAGE, stage);
		tag.putInt(TAG_FLIP_REMAINING, remaining);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide) {
			// Запускаем процесс переворота, если он ещё не активен
			if (!stack.hasTag() || !stack.getTag().contains(TAG_FLIP_STAGE)) {
				setFlip(stack, 1, 1); // старт: стадия 1, 20 тиков до следующей
				return InteractionResultHolder.success(stack);
			}
		}

		// В остальных случаях передаём управление родительскому методу
		return super.use(level, player, hand);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (newStack.getItem() == getOtherSide(oldStack.getItem())) return false;
		if (oldStack.getItem() == getOtherSide(newStack.getItem())) return false;
		return !ItemStack.isSameItem(oldStack, newStack);
	}


	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				ClientRPGtooltipHandler.appendHoverText(pStack, pLevel, pTooltip, pFlag, this)
		);
	}

	@Override
	public boolean hasFeatures(ItemStack item) {
		return false;
	}

	@Override
	public boolean hasComment(ItemStack item) {
		return false;
	}

	@Override
	public String getFirstPredicate(ItemStack item) {
		return Minecraft.getInstance().options.keyUse.getKey().getDisplayName().getString();
	}
}