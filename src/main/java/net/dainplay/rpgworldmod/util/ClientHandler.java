package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.custom.EnderEyeViewEntity;
import net.dainplay.rpgworldmod.item.custom.LivingWoodBowItem;
import net.dainplay.rpgworldmod.item.custom.NetherStarScrollItem;
import net.dainplay.rpgworldmod.network.ClientRainyChunkData;
import net.dainplay.rpgworldmod.network.ClientVelocityStorage;
import net.dainplay.rpgworldmod.network.EntFaceDestroyProgressPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RPGworldMod.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
	private static BlockPos lastAttackedBlock = null;
	private static int attackTicks = 0;
	private static final int PACKET_INTERVAL = 2;

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			ClientRainyChunkData.tick();
			ClientEyeViewHandler.onClientTick(Minecraft.getInstance());
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			int playerId = mc.player.getId();
			if (ClientVelocityStorage.hasVelocity(playerId)) {
				if (!mc.player.isPassenger()) {
					Vec3 velocity = ClientVelocityStorage.retrieveVelocity(playerId);
					mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(velocity));
					mc.player.fallDistance = 0;
				}
			}
		}
		if (event.phase == TickEvent.Phase.END) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.player == null || minecraft.level == null) return;


			if (minecraft.player.hasEffect(ModEffects.PARANOIA.get())) {
				if (minecraft.level.getGameTime() % 40 == 0) {
					spawnParanoiaParticles(minecraft.player);
				}
			}

			if (minecraft.options.keyAttack.isDown()) {
				HitResult hitResult = minecraft.hitResult;
				if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
					BlockHitResult blockHitResult = (BlockHitResult) hitResult;
					BlockPos pos = blockHitResult.getBlockPos();
					BlockState state = minecraft.level.getBlockState(pos);

					if (state.getBlock() instanceof EntFaceBlock) {
						if (pos.equals(lastAttackedBlock)) {
							attackTicks++;
							if (attackTicks >= PACKET_INTERVAL) {
								attackTicks = 0;

								ModMessages.sendToServer(new EntFaceDestroyProgressPacket(
										pos,
										true,
										minecraft.player.getMainHandItem()
								));
							}
						} else {
							lastAttackedBlock = pos;
							attackTicks = 0;

							ModMessages.sendToServer(new EntFaceDestroyProgressPacket(
									pos,
									true,
									minecraft.player.getMainHandItem()
							));
						}
						return;
					}
				}
			}

			lastAttackedBlock = null;
			attackTicks = 0;
		}
	}

	private static void spawnParanoiaParticles(Player player) {
		Level level = player.level();


		double eyeHeight = player.getEyeHeight();
		double eyeY = player.getY() + eyeHeight;

		double distance = 5 + level.getRandom().nextDouble() * 7;


		double angle = level.getRandom().nextDouble() * 2 * Math.PI;


		double yOffset = (level.getRandom().nextDouble() * 2 - 1);


		double xOffset = distance * Math.cos(angle);
		double zOffset = distance * Math.sin(angle);

		double targetX = player.getX() + xOffset;
		double targetY = eyeY + yOffset;
		double targetZ = player.getZ() + zOffset;


		double dx = targetX - player.getX();
		double dy = targetY - player.getY();
		double dz = targetZ - player.getZ();
		double distSquared = dx * dx + dy * dy + dz * dz;

		if (distSquared < 25) {
			return;
		}


		if (isVisibleFromPlayer(player, targetX, targetY, targetZ)) {
			double px = targetX + (level.getRandom().nextDouble() - 0.5) * 0.5;
			double py = targetY + (level.getRandom().nextDouble() - 0.5) * 0.5;
			double pz = targetZ + (level.getRandom().nextDouble() - 0.5) * 0.5;

			level.addParticle(ModParticles.PARANOIA_EYE.get(),
					px, py, pz,
					0, 0, 0);
		}
	}

	private static boolean isVisibleFromPlayer(Player player, double x, double y, double z) {
		Level level = player.level();


		Vec3 start = player.getEyePosition();


		Vec3 end = new Vec3(x, y, z);


		ClipContext context = new ClipContext(
				start,
				end,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				null
		);

		BlockHitResult result = level.clip(context);


		return result.getType() == HitResult.Type.MISS ||
				result.getLocation().distanceToSqr(end) < 0.25;
	}

	@SubscribeEvent
	public static void updateBowFOV(ComputeFovModifierEvent event) {
		Player player = event.getPlayer();
		if (player.isUsingItem()) {
			Item useItem = player.getUseItem().getItem();
			if (useItem instanceof LivingWoodBowItem) {
				float f = player.getTicksUsingItem() / 20.0F;
				f = f > 1.0F ? 1.0F : f * f;
				event.setNewFovModifier((float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, (event.getFovModifier() * (1.0F - f * 0.15F))));
			}
			if ((useItem instanceof NetherStarScrollItem && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DESTRUCTION.get(), player.getUseItem()) > 0 && player.getTicksUsingItem() <= 40)) {
				float f = player.getTicksUsingItem() / 40.0F;
				f = f > 1.0F ? 1.0F : f * f;
				event.setNewFovModifier((float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, (event.getFovModifier() * (1.0F - f * 0.15F))));
			}
		}
	}

	@SubscribeEvent
	public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
		if (ClientEyeViewHandler.isActive()) {
			if (event.getOverlay() != VanillaGuiOverlay.RECORD_OVERLAY.type() &&
					event.getOverlay() != VanillaGuiOverlay.CHAT_PANEL.type()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
		if (ClientEyeViewHandler.isActive() && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE) {
			event.setCanceled(true);
		}
	}

	private static boolean isInvertLoadedByUs = false;
	private static final ResourceLocation INVERT_SHADER = new ResourceLocation("shaders/post/invert.json");

	@SubscribeEvent
	public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
		Minecraft mc = Minecraft.getInstance();
		GameRenderer gameRenderer = mc.gameRenderer;
		Entity cameraEntity = mc.getCameraEntity();


		if (gameRenderer.currentEffect() == null) {
			isInvertLoadedByUs = false;
		}

		boolean isOurEntity = cameraEntity instanceof EnderEyeViewEntity;
		boolean isEnderman = cameraEntity instanceof EnderMan;

		if (isOurEntity) {
			if (gameRenderer.currentEffect() == null || !isInvertLoadedByUs) {
				gameRenderer.loadEffect(INVERT_SHADER);
				isInvertLoadedByUs = true;
			}
		} else if (isEnderman) {
			if (isInvertLoadedByUs) {
				isInvertLoadedByUs = false;
			}
		} else {
			if (isInvertLoadedByUs) {
				gameRenderer.shutdownEffect();
				isInvertLoadedByUs = false;
			}
		}
	}
}