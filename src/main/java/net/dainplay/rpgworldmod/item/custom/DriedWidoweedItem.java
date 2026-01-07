package net.dainplay.rpgworldmod.item.custom;

import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.mana.PlayerManaProvider;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DriedWidoweedItem extends Item {

	// Конфигурация для частиц (аналогично примеру)
	private static final float FORWARD_OFFSET = 0.2f;
	private static final float DOWN_OFFSET = 0.1f;
	private static final Vector3f BREATH_COLOR = new Vector3f(0.35f, 0.35f, 0.47f); // Светло-синий цвет
	private static final float BREATH_SIZE = 0.5f;

	public DriedWidoweedItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack pStack) {
		return UseAnim.TOOT_HORN;
	}

	@Override
	public int getUseDuration(ItemStack pStack) {
		return 20;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		// Потребляем предмет после использования
		if (entity instanceof ServerPlayer player) {
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
				player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
					if(mana.getMana() == 0)
						ModAdvancements.SMOKE_WIDOWEED_TRIGGER.trigger(player);
					mana.addMana(player, 4);
				});
			}
		}
		if (entity instanceof Player player) {
			spawnBreathParticles(level, player);
			player.getCooldowns().addCooldown(this, 20);
			player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
		}
		level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
				SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS,
				0.5F, 0.8F + level.random.nextFloat() * 0.4F);

		// Создаем частицы дыма во время использования

		return stack;
	}

	// Метод для спавна частиц дыхания (вызывается на сервере)
	private void spawnBreathParticles(Level level, Player player) {
		// Получаем позицию головы игрока
		Vec3 headPos = new Vec3(player.getX(), player.getEyeY(), player.getZ());

		// Получаем направление взгляда
		Vec3 look = player.getLookAngle().normalize();

		// Вычисляем точку спавна частиц
		Vec3 forward = look.scale(FORWARD_OFFSET);
		Vec3 down = new Vec3(0, -DOWN_OFFSET, 0);
		Vec3 spawnPos = headPos.add(forward).add(down);

		// Используем RandomSource из уровня
		RandomSource random = level.random;

		// Количество частиц (1 или 2, как в примере)
		int count = random.nextInt(3) == 0 ? 2 : 1;

		for (int i = 0; i < 12; i++) {
			// Случайное смещение позиции
			double ox = (random.nextDouble() - 0.5) * 0.08;
			double oy = (random.nextDouble() - 0.5) * 0.04;
			double oz = (random.nextDouble() - 0.5) * 0.08;

			// Вычисляем скорость частиц
			double vx = look.x * 0.003 + (random.nextDouble() - 0.5) * 0.002;
			double vy = Math.max(0, look.y * 0.001) + (random.nextDouble() - 0.5) * 0.002;
			double vz = look.z * 0.003 + (random.nextDouble() - 0.5) * 0.002;

			// Создаем частицу пыли с указанным цветом и размером
			DustParticleOptions dustParticle = new DustParticleOptions(BREATH_COLOR, BREATH_SIZE);

			// Спавним частицу на сервере (она автоматически синхронизируется с клиентами)
			level.addParticle(dustParticle,
					spawnPos.x + ox, spawnPos.y + oy, spawnPos.z + oz,
					vx, vy, vz);
		}

		// Также можно добавить дополнительные частицы дыма для эффекта
		/*for (int i = 0; i < 5; i++) {
			double offsetX = (random.nextDouble() - 0.5) * 0.1;
			double offsetY = random.nextDouble() * 0.05;
			double offsetZ = (random.nextDouble() - 0.5) * 0.1;

			level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
					spawnPos.x + offsetX,
					spawnPos.y + offsetY,
					spawnPos.z + offsetZ,
					0.0, 0.01 + random.nextDouble() * 0.02, 0.0);
		}*/
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		// Начинаем использование предмета
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				RPGSounds.INHALE.get(), SoundSource.PLAYERS,
				0.5F, 0.8F + level.random.nextFloat() * 0.4F);
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(itemstack);
	}
}