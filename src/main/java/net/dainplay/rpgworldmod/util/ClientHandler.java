package net.dainplay.rpgworldmod.util;

import net.dainplay.rpgworldmod.RPGworldMod;
import net.dainplay.rpgworldmod.block.custom.EntFaceBlock;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.item.custom.LivingWoodBowItem;
import net.dainplay.rpgworldmod.network.ClientRainyChunkData;
import net.dainplay.rpgworldmod.network.EntFaceDestroyProgressPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.network.SyncEffectPacket;
import net.dainplay.rpgworldmod.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
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
		}
		if (event.phase == TickEvent.Phase.END) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.player == null || minecraft.level == null) return;

			// Проверяем наличие эффекта отравления
			if (minecraft.player.hasEffect(ModEffects.PARANOIA.get())) {
				// Спавним частицы с определенной частотой
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
								// Отправляем пакет с предметом в руке игрока
								ModMessages.sendToServer(new EntFaceDestroyProgressPacket(
										pos,
										true,
										minecraft.player.getMainHandItem() // Добавляем предмет
								));
							}
						} else {
							lastAttackedBlock = pos;
							attackTicks = 0;
							// Отправляем сразу первый пакет с предметом
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

		// Уровень глаз игрока
		double eyeHeight = player.getEyeHeight();
		double eyeY = player.getY() + eyeHeight;

		double distance = 5 + level.getRandom().nextDouble() * 7; // От 5 до 12

		// Случайный угол от 0 до 360 градусов
		double angle = level.getRandom().nextDouble() * 2 * Math.PI;

		// Случайное смещение по Y в пределах ±1 блока от уровня глаз
		double yOffset = (level.getRandom().nextDouble() * 2 - 1); // От -1 до 1

		// Вычисляем координаты относительно игрока
		double xOffset = distance * Math.cos(angle);
		double zOffset = distance * Math.sin(angle);

		double targetX = player.getX() + xOffset;
		double targetY = eyeY + yOffset;
		double targetZ = player.getZ() + zOffset;

		// Проверяем, находится ли точка в запретной зоне (радиус 5 блоков в 3D)
		double dx = targetX - player.getX();
		double dy = targetY - player.getY();
		double dz = targetZ - player.getZ();
		double distSquared = dx * dx + dy * dy + dz * dz;

		if (distSquared < 25) { // 5^2 = 25
			return;
		}

		// Проверяем видимость от глаз игрока до точки
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

		// Начальная точка - глаза игрока
		Vec3 start = player.getEyePosition();

		// Конечная точка - целевая позиция
		Vec3 end = new Vec3(x, y, z);

		// Проверяем, нет ли препятствий на пути
		ClipContext context = new ClipContext(
				start,
				end,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				null
		);

		BlockHitResult result = level.clip(context);

		// Если луч не достиг цели (уперся в блок), точка не видна
		return result.getType() == HitResult.Type.MISS ||
				result.getLocation().distanceToSqr(end) < 0.25; // Допуск 0.5 блока (0.5^2 = 0.25)
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
		}
	}
}