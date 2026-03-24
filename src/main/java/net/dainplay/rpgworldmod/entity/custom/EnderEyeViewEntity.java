package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.network.C2SEyeDestroyPacket;
import net.dainplay.rpgworldmod.network.ModMessages;
import net.dainplay.rpgworldmod.util.ClientEyeViewHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.UUID;

public class EnderEyeViewEntity extends Entity implements ItemSupplier {
	private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(EnderEyeViewEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(EnderEyeViewEntity.class, EntityDataSerializers.FLOAT);

	private double tx;
	private double ty;
	private double tz;
	private double horizontalDistance;
	private double yDistance;
	private int life;
	private UUID ownerUUID;

	// Для интерполяции углов
	private float prevYaw;
	private float prevPitch;

	public EnderEyeViewEntity(EntityType<?> type, Level level) {
		super(type, level);
	}

	public EnderEyeViewEntity(Level level, Player owner, double x, double y, double z) {
		this(ModEntities.ENDER_EYE_VIEW.get(), level);
		this.setPos(x, y, z);
		this.ownerUUID = owner.getUUID();
		this.setYaw(owner.getYRot());
		this.setPitch(owner.getXRot());
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_YAW, 0.0f);
		this.getEntityData().define(DATA_PITCH, 0.0f);
	}

	public void setYaw(float yaw) {
		this.getEntityData().set(DATA_YAW, yaw);
		this.setYRot(yaw);
	}

	public float getYaw() {
		return this.getEntityData().get(DATA_YAW);
	}

	public void setPitch(float pitch) {
		this.getEntityData().set(DATA_PITCH, pitch);
		this.setXRot(pitch);
	}

	public float getPitch() {
		return this.getEntityData().get(DATA_PITCH);
	}

	// Интерполированные углы для плавной камеры
	public float getViewYRot(float partialTicks) {
		return Mth.lerp(partialTicks, prevYaw, getYaw());
	}

	public float getViewXRot(float partialTicks) {
		return Mth.lerp(partialTicks, prevPitch, getPitch());
	}

	public Player getOwner() {
		return ownerUUID != null ? this.level().getPlayerByUUID(ownerUUID) : null;
	}

	@Override
	public ItemStack getItem() {
		return new ItemStack(Items.ENDER_EYE);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double pDistance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
		if (Double.isNaN(d0)) d0 = 4.0D;
		d0 *= 64.0D;
		return pDistance < d0 * d0;
	}

	public void signalTo(Vec3 pos) {
		this.tx = pos.x;
		this.ty = pos.y;
		this.tz = pos.z;
		this.yDistance = Math.abs(pos.y - this.getY());
		double dx = pos.x - this.getX();
		double dz = pos.z - this.getZ();
		this.horizontalDistance = Math.sqrt(dx * dx + dz * dz);
		this.life = 0;
	}

	@Override
	public void lerpMotion(double x, double y, double z) {
		this.setDeltaMovement(x, y, z);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			double d0 = Math.sqrt(x * x + z * z);
			this.setYRot((float) (Mth.atan2(x, z) * (180F / (float) Math.PI)));
			this.setXRot((float) (Mth.atan2(y, d0) * (180F / (float) Math.PI)));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}
	}

	public void explode() {
		this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
		this.discard();
		this.level().levelEvent(2003, this.blockPosition(), 0);
	}

	@Override
	public void tick() {
		super.tick(); // Сохранит старые углы в yRotO/xRotO

		// Обновляем интерполяционные поля
		prevYaw = getYaw();
		prevPitch = getPitch();

		Vec3 motion = this.getDeltaMovement();
		double d0 = this.getX() + motion.x;
		double d1 = this.getY() + motion.y;
		double d2 = this.getZ() + motion.z;


		if (level().isClientSide()) {

			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
						if (this == ClientEyeViewHandler.getActiveEye()) {
							Player owner = Minecraft.getInstance().player;
							if (owner != null) {
								owner.zza = 0;
								owner.yya = 0;
								owner.xxa = 0;
								owner.setJumping(false);
								owner.setSprinting(false);
							}
						}
					}
			);
		}
		if (!this.level().isClientSide) {
			Player owner = getOwner();
			if (owner != null && owner.isAlive()) {
				owner.zza = 0;
				owner.yya = 0;
				owner.xxa = 0;
				owner.setJumping(false);
				owner.setSprinting(false);
			}
			double xLeft = this.tx - d0;
			double yLeft = this.ty - d1;
			double zLeft = this.tz - d2;
			float horizontalDistanceLeft = (float) Math.sqrt(xLeft * xLeft + zLeft * zLeft);
			float f1 = (float) Mth.atan2(zLeft, xLeft);
			double d6 = Mth.lerp(0.0025D, motion.horizontalDistance(), (double) horizontalDistanceLeft);
			double d7 = Mth.lerp(0.0025D, motion.y, (double) yLeft);
			if (this.horizontalDistance > 0)
				d6 *= Math.max(0, horizontalDistanceLeft / this.horizontalDistance);
			else
				d6 = 0;
			if (this.yDistance > 0)
				d7 *= Math.min(1.0, Math.abs(yLeft) / this.yDistance);
			else
				d7 = 0;
			int OSCILLATION_START = 60;
			float MAX_AMPLITUDE = 0.05f;
			float FREQUENCY = 0.4f; // радиан на тик
			float amplitude = 0;
			float sinValue = 0;
			if (life >= OSCILLATION_START) {
				if (life >= 90) {
					if (d6 < 0.05) d6 = 0;
					if (d7 < 0.05) d7 = 0;
				}
				int time = life - OSCILLATION_START;
				amplitude = (float) time / (120 - OSCILLATION_START) * MAX_AMPLITUDE;
				amplitude = Mth.clamp(amplitude, 0.0f, MAX_AMPLITUDE);
				sinValue = Mth.sin(time * FREQUENCY);
			}
			motion = new Vec3(Math.cos((double) f1) * d6, d7 + amplitude * sinValue, Math.sin((double) f1) * d6);

			this.setDeltaMovement(motion);
		}

		if (this.isInWater()) {
			for (int i = 0; i < 4; ++i) {
				this.level().addParticle(ParticleTypes.BUBBLE,
						d0 - motion.x * 0.25D, d1 - motion.y * 0.25D, d2 - motion.z * 0.25D,
						motion.x, motion.y, motion.z);
			}
		} else {
			this.level().addParticle(ParticleTypes.PORTAL,
					d0 - motion.x * 0.25D + this.random.nextDouble() * 0.6D - 0.3D,
					d1 - motion.y * 0.25D - 0.5D,
					d2 - motion.z * 0.25D + this.random.nextDouble() * 0.6D - 0.3D,
					motion.x, motion.y, motion.z);
		}

		if (!this.level().isClientSide) {
			this.setPos(d0, d1, d2);
			++this.life;
			if (this.life > 120) {
				this.explode();
			}
		} else {
			this.setPos(d0, d1, d2);
			// Проверяем выход по Shift на клиенте
			if (ClientEyeViewHandler.isActive() && ClientEyeViewHandler.getActiveEye() == this) {
				Minecraft mc = Minecraft.getInstance();
				if (mc.options.keyShift.isDown()) {
					ModMessages.sendToServer(new C2SEyeDestroyPacket(this.getId()));
					ClientEyeViewHandler.clear();
				}
			}
		}

	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID("Owner")) ownerUUID = tag.getUUID("Owner");
	}

	@Override
	public float getLightLevelDependentMagicValue() {
		return 1.0F;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		// Разрешаем обновление углов всегда
		super.onSyncedDataUpdated(key);
	}
}