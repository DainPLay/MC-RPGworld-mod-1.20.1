package net.dainplay.rpgworldmod.entity.projectile;

import net.dainplay.rpgworldmod.block.custom.LivingWoodLogBlock;
import net.dainplay.rpgworldmod.block.custom.RieLeavesBlock;
import net.dainplay.rpgworldmod.block.entity.custom.EntFaceBlockEntity;
import net.dainplay.rpgworldmod.enchantment.ModEnchantments;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class ThrownDrillSpear extends AbstractArrow {
	private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownDrillSpear.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownDrillSpear.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> ID_POWER = SynchedEntityData.defineId(ThrownDrillSpear.class, EntityDataSerializers.FLOAT);
	private ItemStack drillSpearItem = new ItemStack(ModItems.DRILL_SPEAR.get());
	private boolean dealtDamage;
	public int clientSideReturnTridentTickCount;

	public ThrownDrillSpear(EntityType<? extends ThrownDrillSpear> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	public ThrownDrillSpear(Level pLevel, LivingEntity pShooter, ItemStack pStack) {
		super(ModEntities.DRILL_SPEAR_PROJECTILE.get(), pShooter, pLevel);
		this.drillSpearItem = pStack.copy();
		this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(pStack));
		this.entityData.set(ID_FOIL, pStack.hasFoil());
		this.entityData.set(ID_POWER, 1F);
		if (EnchantmentHelper.getEnchantments(pStack).containsKey(Enchantments.BLOCK_EFFICIENCY))
			this.entityData.set(ID_POWER, 1F + 1.5F * (EnchantmentHelper.getEnchantments(pStack).get(Enchantments.BLOCK_EFFICIENCY)));
	}

	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ID_LOYALTY, (byte) 0);
		this.entityData.define(ID_FOIL, false);
		this.entityData.define(ID_POWER, (float) 0F);
	}


	private void checkCollision() {
		HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		if (hitresult.getType() == HitResult.Type.MISS || !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult))
			this.onHit(hitresult);
	}

	public void tick() {
		if (this.entityData.get(ID_POWER) > 0F) {
			Vec3 delta = this.getDeltaMovement();
			double distance = delta.length();

			double radius = this.getBbWidth() * 0.5D;

			int samples = Math.max(1, Mth.ceil(distance / 0.25D));

			java.util.Set<BlockPos> visited = new java.util.HashSet<>();

			for (int i = 0; i <= samples; i++) {
				double t = (double) i / (double) samples;
				double x = this.getX() + delta.x * t;
				double y = this.getY() + delta.y * t;
				double z = this.getZ() + delta.z * t;

				AABB sampleBox = new AABB(
						x - radius, y - radius, z - radius,
						x + radius, y + radius, z + radius
				);

				for (BlockPos pos : BlockPos.betweenClosed(
						Mth.floor(sampleBox.minX), Mth.floor(sampleBox.minY), Mth.floor(sampleBox.minZ),
						Mth.floor(sampleBox.maxX), Mth.floor(sampleBox.maxY), Mth.floor(sampleBox.maxZ))) {

					if (visited.add(pos.immutable())) {
						this.processBlock(pos);
					}
				}

				if (this.entityData.get(ID_POWER) <= 0F) break;
			}
		}

		if (this.inGroundTime > 4) {
			this.dealtDamage = true;
		}

		Entity entity = this.getOwner();
		int i = this.entityData.get(ID_LOYALTY);
		if (i > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
			if (!this.isAcceptibleReturnOwner()) {
				if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
					this.spawnAtLocation(this.getPickupItem(), 0.1F);
				}
				this.discard();
			} else {
				this.setNoPhysics(true);
				Vec3 vec3 = entity.getEyePosition().subtract(this.position());
				this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015D * (double) i, this.getZ());
				if (this.level().isClientSide) {
					this.yOld = this.getY();
				}
				double d0 = 0.05D * (double) i;
				this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vec3.normalize().scale(d0)));
				if (this.clientSideReturnTridentTickCount == 0) {
					this.playSound(RPGSounds.DRILL_SPEAR_RETURN.get(), 10.0F, 1.0F);
				}
				++this.clientSideReturnTridentTickCount;
			}
		}

		super.tick();
	}

	private void processBlock(BlockPos pos) {
		Level level = this.level();
		BlockState blockstate = level.getBlockState(pos);
		if (blockstate.isAir()) return;

		Block block = blockstate.getBlock();

		boolean isEntBlock = (block instanceof LivingWoodLogBlock lw && lw.isRelatedToEnt(blockstate) != 0)
				|| (block instanceof RieLeavesBlock rl && rl.isRelatedToEnt(blockstate) != 0);

		if (isEntBlock) {
			for (int x = -16; x <= 16; x++) {
				for (int y = -16; y <= 16; y++) {
					for (int z = -16; z <= 16; z++) {
						BlockPos checkPos = pos.offset(x, y, z);
						if (level.getBlockEntity(checkPos) instanceof EntFaceBlockEntity entEntity) {
							if (entEntity.getRelatedBlocks().contains(pos)) {
								entEntity.onRelatedBlockAttacked(pos);
							}
						}
					}
				}
			}
		}

		if (level.getBlockEntity(pos) instanceof EntFaceBlockEntity entEntity) {
			entEntity.onRelatedBlockAttacked(pos);
		}

		if (blockstate.getBlock().getExplosionResistance(blockstate, level, pos, null) >= 20F) return;

		if (level.isClientSide()) {
			level.addDestroyBlockEffect(pos, blockstate);
			this.entityData.set(ID_POWER, this.entityData.get(ID_POWER) - blockstate.getBlock().defaultDestroyTime());
			this.playSound(RPGSounds.DRILL.get(), 0.5F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
			return;
		}

		if (!(level instanceof ServerLevel serverLevel)) return;

		ItemStack silkTouchTool = new ItemStack(Items.DIAMOND_PICKAXE);
		var enchants = EnchantmentHelper.getEnchantments(drillSpearItem);
		if (enchants.containsKey(Enchantments.SILK_TOUCH))
			silkTouchTool.enchant(Enchantments.SILK_TOUCH, 1);
		if (enchants.containsKey(Enchantments.BLOCK_FORTUNE))
			silkTouchTool.enchant(Enchantments.BLOCK_FORTUNE, enchants.get(Enchantments.BLOCK_FORTUNE));

		BlockEntity blockEntity = level.getBlockEntity(pos);
		LootParams.Builder builder = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.TOOL, silkTouchTool);
		if (blockEntity != null) {
			builder.withParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
		}

		List<ItemStack> drops = blockstate.getDrops(builder);
		int fortuneLevel = enchants.getOrDefault(Enchantments.BLOCK_FORTUNE, 0);
		int exp = blockstate.getExpDrop(level, level.random, pos, fortuneLevel, 0);

		for (ItemStack itemStack : drops) {
			if (enchants.containsKey(ModEnchantments.COLLECTION.get()) && this.getOwner() instanceof Player player) {
				if (!player.getInventory().add(itemStack)) {
					Block.popResource(level, pos, itemStack);
				}
				if (player instanceof ServerPlayer && !enchants.containsKey(Enchantments.SILK_TOUCH))
					player.giveExperiencePoints(exp);
			} else {
				Block.popResource(level, pos, itemStack);
				if (!enchants.containsKey(Enchantments.SILK_TOUCH))
					blockstate.getBlock().popExperience(serverLevel, pos, exp);
			}
		}

		level.destroyBlock(pos, false);
		this.entityData.set(ID_POWER, this.entityData.get(ID_POWER) - blockstate.getBlock().defaultDestroyTime());
		this.playSound(RPGSounds.DRILL.get(), 0.5F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
	}

	private boolean isAcceptibleReturnOwner() {
		Entity entity = this.getOwner();
		if (entity != null && entity.isAlive()) {
			return !(entity instanceof ServerPlayer) || !entity.isSpectator();
		} else {
			return false;
		}
	}

	public ItemStack getPickupItem() {
		return this.drillSpearItem.copy();
	}

	public boolean isFoil() {
		return this.entityData.get(ID_FOIL);
	}


	@Nullable
	protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
		return this.dealtDamage ? null : super.findHitEntity(pStartVec, pEndVec);
	}


	protected void onHitEntity(EntityHitResult pResult) {
		Entity entity = pResult.getEntity();
		float f = 3.0F;

		Entity entity1 = this.getOwner();
		DamageSource damagesource = this.damageSources().trident(this, (Entity) (entity1 == null ? this : entity1));
		this.dealtDamage = true;
		SoundEvent soundevent = RPGSounds.DRILL_SPEAR_HIT.get();
		if (entity.hurt(damagesource, f)) {
			if (entity.getType() == EntityType.ENDERMAN) {
				return;
			}

			if (entity instanceof LivingEntity) {
				LivingEntity livingentity1 = (LivingEntity) entity;
				if (entity1 instanceof LivingEntity) {
					EnchantmentHelper.doPostHurtEffects(livingentity1, entity1);
					EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity1);
				}

				this.doPostHurtEffects(livingentity1);
			}
		}

		this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
		float f1 = 1.0F;

		this.playSound(soundevent, f1, 1.0F);
	}

	protected boolean tryPickup(Player pPlayer) {
		return super.tryPickup(pPlayer) || this.isNoPhysics() && this.ownedBy(pPlayer) && pPlayer.getInventory().add(this.getPickupItem());
	}


	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return RPGSounds.DRILL_SPEAR_HIT_GROUND.get();
	}


	public void playerTouch(Player pEntity) {
		if (this.ownedBy(pEntity) || this.getOwner() == null) {
			super.playerTouch(pEntity);
		}

	}


	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		if (pCompound.contains("Drill Spear", 10)) {
			this.drillSpearItem = ItemStack.of(pCompound.getCompound("Drill Spear"));
		}

		this.dealtDamage = pCompound.getBoolean("DealtDamage");
		this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.drillSpearItem));
	}

	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		pCompound.put("Drill Spear", this.drillSpearItem.save(new CompoundTag()));
		pCompound.putBoolean("DealtDamage", this.dealtDamage);
	}

	public void tickDespawn() {
		int i = this.entityData.get(ID_LOYALTY);
		if (this.pickup != AbstractArrow.Pickup.ALLOWED || i <= 0) {
			super.tickDespawn();
		}

	}

	protected float getWaterInertia() {
		return 0.99F;
	}

	public boolean shouldRender(double pX, double pY, double pZ) {
		return true;
	}
}