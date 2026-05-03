package net.dainplay.rpgworldmod.mixin;

import com.mojang.blaze3d.Blaze3D;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.dainplay.rpgworldmod.item.custom.EmberScrollItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.network.ClientAdditionalHealthCostData;
import net.dainplay.rpgworldmod.network.ClientEntPositionData;
import net.dainplay.rpgworldmod.util.BeaconSpellStarMenuHandler;
import net.dainplay.rpgworldmod.util.ClientEyeViewHandler;
import net.dainplay.rpgworldmod.util.RecolorWoolMenuHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Shadow
	private double xpos;
	@Shadow
	private double ypos;
	@Shadow
	private double accumulatedDX;
	@Shadow
	private double accumulatedDY;

	@Unique
	private double frozenAccumulatedDX = 0;
	@Unique
	private double frozenAccumulatedDY = 0;

	@Unique
	private float targetYaw = 0;
	@Unique
	private float targetPitch = 0;
	@Unique
	private float currentYaw = 0;
	@Unique
	private float currentPitch = 0;
	@Unique
	private boolean isTracking = false;

	@Shadow
	private double lastMouseEventTime;
	@Shadow
	private SmoothDouble smoothTurnX;
	@Shadow
	private SmoothDouble smoothTurnY;


	@Unique
	private final SmoothDouble customSmoothTurnX = new SmoothDouble();
	@Unique
	private final SmoothDouble customSmoothTurnY = new SmoothDouble();
	@Unique
	private boolean wasDestructionActive = false;

	@Inject(method = "turnPlayer", at = @At(value = "HEAD"), cancellable = true)
	private void onTurnPlayer(CallbackInfo ci) {
		Player player = Minecraft.getInstance().player;
		if (player == null) return;


		if (BeaconSpellStarMenuHandler.isActive()) {
			Minecraft mc = Minecraft.getInstance();
			double d0 = Blaze3D.getTime();
			double d1 = d0 - lastMouseEventTime;
			lastMouseEventTime = d0;

			double d4 = mc.options.sensitivity().get() * 0.6 + 0.2;
			double d5 = d4 * d4 * d4;
			double d6 = d5 * 8.0;
			double d2, d3;
			if (mc.options.smoothCamera) {
				double d7 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d6, d1 * d6);
				double d8 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d6, d1 * d6);
				d2 = d7;
				d3 = d8;
			} else {
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();
				d2 = this.accumulatedDX * d6;
				d3 = this.accumulatedDY * d6;
			}
			accumulatedDX = 0;
			accumulatedDY = 0;

			BeaconSpellStarMenuHandler.onMouseMove(d2, d3);
			ci.cancel();
			return;
		}


		if (RecolorWoolMenuHandler.isActive()) {
			Minecraft mc = Minecraft.getInstance();
			double d0 = Blaze3D.getTime();
			double d1 = d0 - lastMouseEventTime;
			lastMouseEventTime = d0;

			double d4 = mc.options.sensitivity().get() * 0.6 + 0.2;
			double d5 = d4 * d4 * d4;
			double d6 = d5 * 8.0;
			double d2, d3;
			if (mc.options.smoothCamera) {
				double d7 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d6, d1 * d6);
				double d8 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d6, d1 * d6);
				d2 = d7;
				d3 = d8;
			} else {
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();
				d2 = this.accumulatedDX * d6;
				d3 = this.accumulatedDY * d6;
			}
			accumulatedDX = 0;
			accumulatedDY = 0;

			RecolorWoolMenuHandler.onMouseMove(d2, d3);
			ci.cancel();
			return;
		}


		if (ClientEyeViewHandler.isActive()) {
			Minecraft mc = Minecraft.getInstance();
			EnderEyeViewEntity eye = ClientEyeViewHandler.getActiveEye();
			if (eye != null && eye.isAlive()) {
				double d0 = Blaze3D.getTime();
				double d1 = d0 - lastMouseEventTime;
				lastMouseEventTime = d0;

				double d4 = mc.options.sensitivity().get() * 0.6 + 0.2;
				double d5 = d4 * d4 * d4;
				double d6 = d5 * 8.0;
				double d2, d3;
				if (mc.options.smoothCamera) {
					double d7 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d6, d1 * d6);
					double d8 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d6, d1 * d6);
					d2 = d7;
					d3 = d8;
				} else {
					this.smoothTurnX.reset();
					this.smoothTurnY.reset();
					d2 = this.accumulatedDX * d6;
					d3 = this.accumulatedDY * d6;
				}
				accumulatedDX = 0;
				accumulatedDY = 0;

				int invert = mc.options.invertYMouse().get() ? -1 : 1;
				float yawDelta = (float) d2 * 0.15F;
				float pitchDelta = (float) (d3 * invert) * 0.15F;
				float newYaw = eye.getYaw() + yawDelta;
				float newPitch = eye.getPitch() + pitchDelta;
				newPitch = Mth.clamp(newPitch, -90, 90);
				eye.setYaw(newYaw);
				eye.setPitch(newPitch);

				ci.cancel();
				return;
			} else {
				ClientEyeViewHandler.clear();
			}
		}


		if (player.isUsingItem()) {
			ItemStack usingItem = player.getUseItem();

			if (usingItem.getItem() instanceof NetherStarScrollItem &&
					EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), usingItem) > 0) {
				Minecraft mc = Minecraft.getInstance();
				double d0 = Blaze3D.getTime();
				double d1 = d0 - lastMouseEventTime;
				lastMouseEventTime = d0;


				double d4 = mc.options.sensitivity().get() * 0.6 + 0.2;
				double d5 = d4 * d4 * d4;
				double d6 = d5 * 8.0;


				if (!wasDestructionActive) {
					customSmoothTurnX.reset();
					customSmoothTurnY.reset();
					wasDestructionActive = true;
				}


				double d2 = customSmoothTurnX.getNewDeltaValue(this.accumulatedDX * d6, d1 * d6);
				double d3 = customSmoothTurnY.getNewDeltaValue(this.accumulatedDY * d6, d1 * d6);


				float maxAngleDelta = 1f;
				d2 = Mth.clamp(d2, -maxAngleDelta, maxAngleDelta);
				d3 = Mth.clamp(d3, -maxAngleDelta, maxAngleDelta);


				int invert = mc.options.invertYMouse().get() ? -1 : 1;


				player.turn((float) d2, (float) (d3 * invert));


				this.accumulatedDX = 0;
				this.accumulatedDY = 0;

				ci.cancel();
				return;
			}
		}


		if (wasDestructionActive) {
			wasDestructionActive = false;
		}


		if (player.isUsingItem() &&
				player.getUseItemRemainingTicks() > 0 &&
				player.getUseItem().getItem() instanceof EmberScrollItem &&
				player.getUseItem().getEnchantmentLevel(ModEnchantments.NECROMANCY.get()) > 0 &&
				!player.isShiftKeyDown()) {
			if (ClientAdditionalHealthCostData.hasRotationStored()) {
				float angleDifference = ClientAdditionalHealthCostData.calculateAngleDifference(player);
				int maxHealthCost = 18;
				if (maxHealthCost < 0) maxHealthCost = 0;
				int healthCost = 0;
				if (maxHealthCost > 0) {
					float step = 90.0f / maxHealthCost;
					healthCost = (int) (angleDifference / step);
					healthCost = Math.min(healthCost, maxHealthCost);
				}
				if (healthCost > Mth.ceil(player.getHealth()) - 2)
					healthCost = Math.max(0, Mth.ceil(player.getHealth()) - 2);
				ClientAdditionalHealthCostData.set(healthCost);
			} else {
				ClientAdditionalHealthCostData.storeRotation(player);
			}
		} else if (ClientAdditionalHealthCostData.hasRotationStored()) {
			ClientAdditionalHealthCostData.reset();
		}

		if (player.hasEffect(ModEffects.PARALYSIS.get()) && !player.isCreative() && !player.isSpectator() &&
				player.getEffect(ModEffects.PARALYSIS.get()).getAmplifier() >= 1) {
			this.accumulatedDX = this.frozenAccumulatedDX;
			this.accumulatedDY = this.frozenAccumulatedDY;
			ci.cancel();
			return;
		}

		BlockPos targetPos = ClientEntPositionData.get();
		if (targetPos != null) {
			Vec3 targetVec = new Vec3(ClientEntPositionData.getX(), ClientEntPositionData.getY(), ClientEntPositionData.getZ());
			Vec3 playerVec = player.getEyePosition();

			double dx = targetVec.x - playerVec.x;
			double dy = targetVec.y - playerVec.y;
			double dz = targetVec.z - playerVec.z;

			double distance = Math.sqrt(dx * dx + dz * dz);
			targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
			targetPitch = (float) (-Math.atan2(dy, distance) * (180.0 / Math.PI));

			targetYaw = normalizeAngle(targetYaw);
			targetPitch = normalizeAngle(targetPitch);

			float playerYaw = player.getYRot();
			float playerPitch = player.getXRot();

			if (!isTracking) {
				currentYaw = playerYaw;
				currentPitch = playerPitch;
				isTracking = true;
				Minecraft.getInstance().options.smoothCamera = true;
			}

			float rotationSpeed = 0.12f;
			currentYaw = lerpAngle(currentYaw, targetYaw, rotationSpeed);
			currentPitch = lerpAngle(currentPitch, targetPitch, rotationSpeed);

			player.setYRot(currentYaw);
			player.setXRot(currentPitch);

			this.accumulatedDX = 0;
			this.accumulatedDY = 0;
			this.frozenAccumulatedDX = 0;
			this.frozenAccumulatedDY = 0;

			ci.cancel();
		} else {
			if (isTracking) {
				isTracking = false;
				Minecraft.getInstance().options.smoothCamera = false;
			}
			this.frozenAccumulatedDX = this.accumulatedDX;
			this.frozenAccumulatedDY = this.accumulatedDY;
		}
	}

	@Unique
	private float normalizeAngle(float angle) {
		angle = angle % 360;
		if (angle > 180) {
			angle -= 360;
		} else if (angle < -180) {
			angle += 360;
		}
		return angle;
	}

	@Unique
	private float lerpAngle(float current, float target, float speed) {
		float diff = target - current;
		while (diff > 180) diff -= 360;
		while (diff < -180) diff += 360;
		return current + diff * speed;
	}
}