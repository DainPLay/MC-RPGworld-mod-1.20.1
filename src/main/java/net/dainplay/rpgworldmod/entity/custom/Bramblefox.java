package net.dainplay.rpgworldmod.entity.custom;

import com.google.common.collect.Lists;
import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.util.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.dainplay.rpgworldmod.entity.custom.Bramblefox.Type.*;

public class Bramblefox extends Animal {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Bramblefox.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bramblefox.class, EntityDataSerializers.BYTE);
       private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0 = SynchedEntityData.defineId(Bramblefox.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1 = SynchedEntityData.defineId(Bramblefox.class, EntityDataSerializers.OPTIONAL_UUID);
    static final Predicate<ItemEntity> ALLOWED_ITEMS = (p_28528_) -> !p_28528_.hasPickUpDelay() && p_28528_.isAlive();
    private static final Predicate<Entity> AVOID_PLAYERS = (player) ->
            (!player.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player))
            || ((ModItems.BRAMBLEFOX_SCARF.get().isEquippedBy((LivingEntity) player))
            && checkVisibility(((LivingEntity) player), ModItems.BRAMBLEFOX_SCARF.get()))
            || ((Player)player).getMainHandItem().getItem() == ModItems.BRAMBLEFOX_SCARF.get()
            || ((Player)player).getOffhandItem().getItem() == ModItems.BRAMBLEFOX_SCARF.get();

    static boolean checkVisibility(LivingEntity entity, Item item){

        AtomicBoolean cosmetic = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(entity)
                .ifPresent(handler -> handler.getCurios().forEach((id, stacksHandler) -> {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);
                        NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                        boolean renderable = renderStates.size() > i && renderStates.get(i);

                        if (stack.isEmpty() && renderable) {
                            if (stackHandler.getStackInSlot(i).getItem() == item)
                                cosmetic.set(true);
                        }
                    }
                }));
        return cosmetic.get();
    }
    private float interestedAngle;
    private float interestedAngleO;
    float crouchAmount;
    float crouchAmountO;
    private int ticksSinceEaten;

    public Bramblefox(EntityType<? extends Bramblefox> pEntityType, Level p_28452_) {
        super(pEntityType, p_28452_);
        this.lookControl = new Bramblefox.BramblefoxLookControl();
        this.moveControl = new Bramblefox.BramblefoxMoveControl();
        this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0F);
        this.setCanPickUpLoot(true);
    }
@Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);

    ItemStack itemstack = ModItems.BRAMBLEFOX_BERRIES.get().getDefaultInstance();
    switch (getBramblefoxType()) {
        case STAGE0:
            itemstack.setCount(0); break;
        case STAGE1:
            itemstack.setCount(random.nextInt(5)+1); break;
        case STAGE2:
            itemstack.setCount(random.nextInt(10)+2); break;
        case STAGE3:
            itemstack.setCount(random.nextInt(15)+3); break;
    }
    this.spawnAtLocation(itemstack);

    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRUSTED_ID_0, Optional.empty());
        this.entityData.define(DATA_TRUSTED_ID_1, Optional.empty());
        this.entityData.define(DATA_TYPE_ID, 0);
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new Bramblefox.BramblefoxFloatGoal());
        this.goalSelector.addGoal(2, new Bramblefox.BramblefoxPanicGoal(2.2D));
        this.goalSelector.addGoal(3, new Bramblefox.BramblefoxBreedGoal(1.0D));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.6D, 1.4D, (p_28596_) -> AVOID_PLAYERS.test(p_28596_)  && !this.trusts(p_28596_.getUUID()) && !this.isDefending()));
        this.goalSelector.addGoal(6, new Bramblefox.SeekShelterGoal(1.25D));
        this.goalSelector.addGoal(7, new Bramblefox.SleepGoal());
        this.goalSelector.addGoal(8, new Bramblefox.BramblefoxFollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(10, new Bramblefox.BramblefoxEatBerriesGoal((double)1.2F, 12, 1));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(11, new Bramblefox.BramblefoxSearchForItemsGoal());
        this.goalSelector.addGoal(12, new Bramblefox.BramblefoxLookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(13, new Bramblefox.PerchAndSearchGoal());
    }

    public SoundEvent getEatingSound(ItemStack pItemStack) {
        return RPGSounds.BRAMBLEFOX_EAT.get();
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void aiStep() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            ++this.ticksSinceEaten;
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (this.canEat(itemstack)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack itemstack1 = itemstack.finishUsingItem(this.level(), this);
                    if (!itemstack1.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, itemstack1);
                    }

                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
                    this.playSound(this.getEatingSound(itemstack), 1.0F, 1.0F);
                    this.stageIncrease();
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    this.level().broadcastEntityEvent(this, (byte)45);
                }
            }

            LivingEntity livingentity = this.getTarget();
            if (livingentity == null || !livingentity.isAlive()) {
                this.setIsCrouching(false);
                this.setIsInterested(false);
            }
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }

        super.aiStep();
        if (this.isDefending() && this.random.nextFloat() < 0.05F) {
            this.playSound(RPGSounds.BRAMBLEFOX_AGGRO.get(), 1.0F, 1.0F);
        }

    }

    /**
     * Dead and sleeping entities cannot move
     */
    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    private boolean canEat(ItemStack pItemStack) {
        return isFood(pItemStack) && this.getTarget() == null && this.onGround() && !this.isSleeping();
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
        if (this.random.nextFloat() < 0.2F) {
            float f = this.random.nextFloat();
            ItemStack itemstack;
            if (f < 0.05F) {
                itemstack = new ItemStack(ModItems.FAIRAPIER_SWORD_WILTED.get());
            } else if (f < 0.2F) {
                itemstack = new ItemStack(ModItems.RIE_FRUIT.get());
            } else if (f < 0.4F) {
                itemstack = new ItemStack(ModItems.FAIRAPIER_SEED.get());
            } else if (f < 0.6F) {
                itemstack = new ItemStack(ModBlocks.HOLTS_REFLECTION.get());
            } else if (f < 0.8F) {
                itemstack = new ItemStack(ModItems.PROJECTRUFFLE_ITEM.get());
            } else {
                itemstack = new ItemStack(ModItems.BRAMBLEFOX_BERRIES.get());
            }

            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
        }

    }
    public void handleEntityEvent(byte pId) {
        if (pId == 45) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemstack.isEmpty()) {
                for(int i = 0; i < 8; ++i) {
                    Vec3 vec3 = (new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).xRot(-this.getXRot() * ((float)Math.PI / 180F)).yRot(-this.getYRot() * ((float)Math.PI / 180F));
                    this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemstack), this.getX() + this.getLookAngle().x / 2.0D, this.getY(), this.getZ() + this.getLookAngle().z / 2.0D, vec3.x, vec3.y + 0.05D, vec3.z);
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public Bramblefox getBreedOffspring(ServerLevel p_148912_, AgeableMob p_148913_) {
        Bramblefox fox = ModEntities.BRAMBLEFOX.get().create(p_148912_);
        return fox;
    }
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.getItem() instanceof BoneMealItem && pPlayer.getAbilities().instabuild ) { //Forge: Moved to onSheared
            if (!this.level().isClientSide) {
                this.stageIncrease();
                itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME;
            }
        } else {
            if (this.getBramblefoxType() != STAGE0)
                if (!this.level().isClientSide) {
                this.stageDecrease();
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME; }
            return super.mobInteract(pPlayer, pHand);
        }
    }

    public static boolean checkBramblefoxSpawnRules(EntityType<Bramblefox> p_186214_, LevelAccessor p_186215_, MobSpawnType p_186216_, BlockPos p_186217_, RandomSource p_186218_) {
        return p_186215_.getBlockState(p_186217_.below()).is(BlockTags.FOXES_SPAWNABLE_ON) && isBrightEnoughToSpawn(p_186215_, p_186217_);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        Bramblefox.Type fox$type = Bramblefox.Type.byName("STAGE3");
        boolean flag = false;
        if (pSpawnData instanceof Bramblefox.BramblefoxGroupData) {
            if (((Bramblefox.BramblefoxGroupData)pSpawnData).getGroupSize() >= 2) {
                flag = true;
            }
        } else {
            pSpawnData = new Bramblefox.BramblefoxGroupData(fox$type);
        }

        this.setBramblefoxType(fox$type);
        if (flag) {
            this.setAge(-24000);
        }

        this.populateDefaultEquipmentSlots(pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    protected void usePlayerItem(Player p_148908_, InteractionHand p_148909_, ItemStack p_148910_) {
        if (this.isFood(p_148910_)) {
            this.playSound(this.getEatingSound(p_148910_), 1.0F, 1.0F);
            this.stageIncrease();
        }

        super.usePlayerItem(p_148908_, p_148909_, p_148910_);
    }

    public void stageIncrease(){
            switch (getBramblefoxType()) {
                case STAGE0: this.setBramblefoxType(STAGE1); break;
                case STAGE1: this.setBramblefoxType(STAGE2); break;
                case STAGE2: this.setBramblefoxType(STAGE3); break;
                case STAGE3:
                    this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    ItemStack itemstack = ModItems.BRAMBLEFOX_BERRIES.get().getDefaultInstance();
                    itemstack.setCount(random.nextInt(5)+1);
                    this.spawnAtLocation(itemstack);
                    break;
        }
    }

    public void stageDecrease(){
        ItemStack itemstack = ModItems.BRAMBLEFOX_BERRIES.get().getDefaultInstance();
        itemstack.setCount(random.nextInt(5)+1);
        switch (getBramblefoxType()) {
            case STAGE0: break;
            case STAGE1: this.setBramblefoxType(STAGE0);
                this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.spawnAtLocation(itemstack); break;
            case STAGE2: this.setBramblefoxType(STAGE1);
                this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.spawnAtLocation(itemstack); break;
            case STAGE3:this.setBramblefoxType(STAGE2);
                this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.spawnAtLocation(itemstack); break;
        }
    }
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return this.isBaby() ? pSize.height * 0.85F : 0.4F;
    }

    public Bramblefox.Type getBramblefoxType() {
        return Bramblefox.Type.byId(this.entityData.get(DATA_TYPE_ID));
    }

    private void setBramblefoxType(Bramblefox.Type pType) {
        this.entityData.set(DATA_TYPE_ID, pType.getId());
    }

    List<UUID> getTrustedUUIDs() {
        List<UUID> list = Lists.newArrayList();
        list.add(this.entityData.get(DATA_TRUSTED_ID_0).orElse((UUID)null));
        list.add(this.entityData.get(DATA_TRUSTED_ID_1).orElse((UUID)null));
        return list;
    }

    void addTrustedUUID(@Nullable UUID pUuid) {
        if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(pUuid));
        } else {
            this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(pUuid));
        }

    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        List<UUID> list = this.getTrustedUUIDs();
        ListTag listtag = new ListTag();

        for(UUID uuid : list) {
            if (uuid != null) {
                listtag.add(NbtUtils.createUUID(uuid));
            }
        }

        pCompound.put("Trusted", listtag);
        pCompound.putBoolean("Sleeping", this.isSleeping());
        pCompound.putString("Type", this.getBramblefoxType().getName());
        pCompound.putBoolean("Sitting", this.isSitting());
        pCompound.putBoolean("Crouching", this.isCrouching());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        ListTag listtag = pCompound.getList("Trusted", 11);

        for(int i = 0; i < listtag.size(); ++i) {
            this.addTrustedUUID(NbtUtils.loadUUID(listtag.get(i)));
        }

        this.setSleeping(pCompound.getBoolean("Sleeping"));
        this.setBramblefoxType(Bramblefox.Type.byName(pCompound.getString("Type")));
        this.setSitting(pCompound.getBoolean("Sitting"));
        this.setIsCrouching(pCompound.getBoolean("Crouching"));

    }

    public boolean isSitting() {
        return this.getFlag(1);
    }

    public void setSitting(boolean p_28611_) {
        this.setFlag(1, p_28611_);
    }

    boolean isDefending() {
        return this.getFlag(128);
    }

    void setDefending(boolean p_28623_) {
        this.setFlag(128, p_28623_);
    }

    /**
     * Returns whether player is sleeping or not
     */
    public boolean isSleeping() {
        return this.getFlag(32);
    }

    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        return pPotioneffect.getEffect() == ModEffects.MOSSIOSIS.get() ? false : super.canBeAffected(pPotioneffect);
    }
    void setSleeping(boolean p_28627_) {
        this.setFlag(32, p_28627_);
    }

    private void setFlag(int p_28533_, boolean p_28534_) {
        if (p_28534_) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | p_28533_));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~p_28533_));
        }

    }

    private boolean getFlag(int p_28609_) {
        return (this.entityData.get(DATA_FLAGS_ID) & p_28609_) != 0;
    }

    public boolean canTakeItem(ItemStack pItemstack) {
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(equipmentslot).isEmpty()) {
            return false;
        } else {
            return equipmentslot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
        }
    }

    public boolean canHoldItem(ItemStack pStack) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return itemstack.isEmpty() || this.ticksSinceEaten > 0 && this.isFood(pStack) && !this.isFood(itemstack);
    }

    private void spitOutItem(ItemStack pStack) {
        if (!pStack.isEmpty() && !this.level().isClientSide) {
            ItemEntity itementity = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0D, this.getZ() + this.getLookAngle().z, pStack);
            itementity.setPickUpDelay(40);
            itementity.setThrower(this.getUUID());
            this.playSound(RPGSounds.BRAMBLEFOX_SPIT.get(), 1.0F, 1.0F);
            this.level().addFreshEntity(itementity);
        }
    }

    private void dropItemStack(ItemStack pStack) {
        ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), pStack);
        this.level().addFreshEntity(itementity);
    }

    /**
     * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
     * better.
     */
    protected void pickUpItem(ItemEntity pItemEntity) {
        if(this.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null
        && !this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty())
            return;
        ItemStack itemstack = pItemEntity.getItem();
        if (this.canHoldItem(itemstack)) {
            int i = itemstack.getCount();
            if (i > 1) {
                this.dropItemStack(itemstack.split(i - 1));
            }

            this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
            this.onItemPickup(pItemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack.split(1));
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
            this.ticksSinceEaten = 0;
        }

    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();
        if (this.isEffectiveAi()) {
            boolean flag = this.isInWater();
            if (flag || this.getTarget() != null || this.level().isThundering()) {
                this.wakeUp();
            }

            if (flag || this.isSleeping()) {
                this.setSitting(false);
            }
        }

        this.interestedAngleO = this.interestedAngle;
        if (this.isInterested()) {
            this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
        } else {
            this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
        }

        this.crouchAmountO = this.crouchAmount;
        if (this.isCrouching()) {
            this.crouchAmount += 0.2F;
            if (this.crouchAmount > 3.0F) {
                this.crouchAmount = 3.0F;
            }
        } else {
            this.crouchAmount = 0.0F;
        }

    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isFood(ItemStack pStack) {
        return pStack.is(ModTags.Items.BRAMBLEFOX_FOOD);
    }

    protected void onOffspringSpawnedFromEgg(Player pPlayer, Mob pChild) {
        ((Bramblefox)pChild).addTrustedUUID(pPlayer.getUUID());
    }

    public boolean isPouncing() {
        return this.getFlag(16);
    }

    public void setIsPouncing(boolean p_28613_) {
        this.setFlag(16, p_28613_);
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public boolean isFullyCrouched() {
        return this.crouchAmount == 3.0F;
    }

    public void setIsCrouching(boolean p_28615_) {
        this.setFlag(4, p_28615_);
    }

    public boolean isCrouching() {
        return this.getFlag(4);
    }

    public void setIsInterested(boolean p_28617_) {
        this.setFlag(8, p_28617_);
    }

    public boolean isInterested() {
        return this.getFlag(8);
    }

    public float getHeadRollAngle(float p_28621_) {
        return Mth.lerp(p_28621_, this.interestedAngleO, this.interestedAngle) * 0.11F * (float)Math.PI;
    }

    public float getCrouchAmount(float p_28625_) {
        return Mth.lerp(p_28625_, this.crouchAmountO, this.crouchAmount);
    }

    /**
     * Sets the active target the Task system uses for tracking
     */
    public void setTarget(@Nullable LivingEntity pLivingEntity) {
        if (this.isDefending() && pLivingEntity == null) {
            this.setDefending(false);
        }

        super.setTarget(pLivingEntity);
    }

    protected int calculateFallDamage(float pDistance, float pDamageMultiplier) {
        return Mth.ceil((pDistance - 5.0F) * pDamageMultiplier);
    }

    void wakeUp() {
        this.setSleeping(false);
    }

    void clearStates() {
        this.setIsInterested(false);
        this.setIsCrouching(false);
        this.setSitting(false);
        this.setSleeping(false);
        this.setDefending(false);
    }

    boolean canMove() {
        return !this.isSleeping() && !this.isSitting();
    }

    /**
     * Plays living's sound at its position
     */
    public void playAmbientSound() {
        SoundEvent soundevent = this.getAmbientSound();
        if (soundevent == RPGSounds.BRAMBLEFOX_SCREECH.get()) {
            this.playSound(soundevent, 2.0F, this.getVoicePitch());
        } else {
            super.playAmbientSound();
        }

    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return RPGSounds.BRAMBLEFOX_SLEEP.get();
        } else {
            if (!this.level().isDay() && this.random.nextFloat() < 0.1F) {
                List<Player> list = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D, 16.0D, 16.0D), EntitySelector.NO_SPECTATORS);
                if (list.isEmpty()) {
                    return RPGSounds.BRAMBLEFOX_SCREECH.get();
                }
            }

            return RPGSounds.BRAMBLEFOX_AMBIENT.get();
        }
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RPGSounds.BRAMBLEFOX_HURT.get();
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return RPGSounds.BRAMBLEFOX_DEATH.get();
    }

    boolean trusts(UUID p_28530_) {
        return this.getTrustedUUIDs().contains(p_28530_);
    }

    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        super.dropAllDeathLoot(pDamageSource);
    }
    protected void dropEquipment() { // Forge: move extra drops to dropEquipment to allow them to be captured by LivingDropsEvent
        super.dropEquipment();
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemstack.isEmpty()) {
            this.spawnAtLocation(itemstack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

    }


    public static boolean isPathClear(Fox pFox, LivingEntity pLivingEntity) {
        double d0 = pLivingEntity.getZ() - pFox.getZ();
        double d1 = pLivingEntity.getX() - pFox.getX();
        double d2 = d0 / d1;
        int i = 6;

        for(int j = 0; j < 6; ++j) {
            double d3 = d2 == 0.0D ? 0.0D : d0 * (double)((float)j / 6.0F);
            double d4 = d2 == 0.0D ? d1 * (double)((float)j / 6.0F) : d3 / d2;

            for(int k = 1; k < 4; ++k) {
                if (!pFox.level().getBlockState(BlockPos.containing(pFox.getX() + d4, pFox.getY() + (double)k, pFox.getZ() + d3)).canBeReplaced()) {
                    return false;
                }
            }
        }

        return true;
    }

    public Vec3 getLeashOffset() {
        return new Vec3(0.0D, (double)(0.55F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    public class BramblefoxAlertableEntitiesSelector implements Predicate<LivingEntity> {
        public boolean test(LivingEntity p_28653_) {
            if (p_28653_ instanceof Bramblefox) {
                return false;
            } else if (!(p_28653_ instanceof Chicken) && !(p_28653_ instanceof Rabbit) && !(p_28653_ instanceof Monster)) {
                if (p_28653_ instanceof TamableAnimal) {
                    return !((TamableAnimal)p_28653_).isTame();
                } else if (!(p_28653_ instanceof Player) || !p_28653_.isSpectator() && !((Player)p_28653_).isCreative()) {
                    if (Bramblefox.this.trusts(p_28653_.getUUID())) {
                        return false;
                    } else {
                        return !p_28653_.isSleeping() && !p_28653_.isDiscrete();
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    abstract class BramblefoxBehaviorGoal extends Goal {
        private final TargetingConditions alertableTargeting = TargetingConditions.forCombat().range(12.0D).ignoreLineOfSight().selector(Bramblefox.this.new BramblefoxAlertableEntitiesSelector());

        protected boolean hasShelter() {
            BlockPos blockpos = BlockPos.containing(Bramblefox.this.getX(), Bramblefox.this.getBoundingBox().maxY, Bramblefox.this.getZ());
            return !Bramblefox.this.level().canSeeSky(blockpos) && Bramblefox.this.getWalkTargetValue(blockpos) >= 0.0F;
        }

        protected boolean alertable() {
            return !Bramblefox.this.level().getNearbyEntities(LivingEntity.class, this.alertableTargeting, Bramblefox.this, Bramblefox.this.getBoundingBox().inflate(12.0D, 6.0D, 12.0D)).isEmpty();
        }
    }

    class BramblefoxBreedGoal extends BreedGoal {
        public BramblefoxBreedGoal(double p_28668_) {
            super(Bramblefox.this, p_28668_);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            ((Bramblefox)this.animal).clearStates();
            ((Bramblefox)this.partner).clearStates();
            super.start();
        }

        /**
         * Spawns a baby animal of the same type.
         */
        protected void breed() {
            ServerLevel serverlevel = (ServerLevel)this.level;
            Bramblefox bramblefox = (Bramblefox)this.animal.getBreedOffspring(serverlevel, this.partner);
            final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(animal, partner, bramblefox);
            final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            bramblefox = (Bramblefox) event.getChild();
            if (cancelled) {
                //Reset the "inLove" state for the animals
                this.animal.setAge(6000);
                this.partner.setAge(6000);
                this.animal.resetLove();
                this.partner.resetLove();
                return;
            }
            if (bramblefox != null) {
                ServerPlayer serverplayer = this.animal.getLoveCause();
                ServerPlayer serverplayer1 = this.partner.getLoveCause();
                ServerPlayer serverplayer2 = serverplayer;
                if (serverplayer != null) {
                    bramblefox.addTrustedUUID(serverplayer.getUUID());
                } else {
                    serverplayer2 = serverplayer1;
                }

                if (serverplayer1 != null && serverplayer != serverplayer1) {
                    bramblefox.addTrustedUUID(serverplayer1.getUUID());
                }

                if (serverplayer2 != null) {
                    serverplayer2.awardStat(Stats.ANIMALS_BRED);
                    CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer2, this.animal, this.partner, bramblefox);
                }

                this.animal.setAge(6000);
                this.partner.setAge(6000);
                this.animal.resetLove();
                this.partner.resetLove();
                bramblefox.setAge(-24000);
                bramblefox.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
                serverlevel.addFreshEntityWithPassengers(bramblefox);
                this.level.broadcastEntityEvent(this.animal, (byte)18);
                if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                    this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
                }

            }
        }
    }

    public class BramblefoxEatBerriesGoal extends MoveToBlockGoal {
        private static final int WAIT_TICKS = 40;
        protected int ticksWaited;

        public BramblefoxEatBerriesGoal(double p_28675_, int p_28676_, int p_28677_) {
            super(Bramblefox.this, p_28675_, p_28676_, p_28677_);
        }

        public double acceptedDistance() {
            return 2.0D;
        }

        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        /**
         * Return true to set given position as destination
         */
        protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
            BlockState blockstate = pLevel.getBlockState(pPos);
            return blockstate.is(ModBlocks.MIMOSSA.get());
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            if (this.isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    this.onReachedTarget();
                } else {
                    ++this.ticksWaited;
                }
            } else if (!this.isReachedTarget() && Bramblefox.this.random.nextFloat() < 0.05F) {
                Bramblefox.this.playSound(RPGSounds.BRAMBLEFOX_SNIFF.get(), 1.0F, 1.0F);
            }

            super.tick();
        }

        protected void onReachedTarget() {
            if (Bramblefox.this.getItemBySlot(EquipmentSlot.MAINHAND).getItem() != ModBlocks.MIMOSSA.get().asItem() && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(Bramblefox.this.level(), Bramblefox.this)) {
                    this.pickMimossa();
            }
        }

        private void pickMimossa() {
            Bramblefox.this.playSound(SoundEvents.CROP_BREAK, 1.0F, 1.0F);
            Bramblefox.this.level().destroyBlock(this.blockPos, true, Bramblefox.this);
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return !Bramblefox.this.isSleeping() && super.canUse();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.ticksWaited = 0;
            Bramblefox.this.setSitting(false);
            super.start();
        }
    }

    class BramblefoxFloatGoal extends FloatGoal {
        public BramblefoxFloatGoal() {
            super(Bramblefox.this);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            super.start();
            Bramblefox.this.clearStates();
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return Bramblefox.this.isInWater() && Bramblefox.this.getFluidHeight(FluidTags.WATER) > 0.25D || Bramblefox.this.isInLava();
        }
    }

    class BramblefoxFollowParentGoal extends FollowParentGoal {
        private final Bramblefox fox;

        public BramblefoxFollowParentGoal(Bramblefox p_28696_, double p_28697_) {
            super(p_28696_, p_28697_);
            this.fox = p_28696_;
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return !this.fox.isDefending() && super.canUse();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return !this.fox.isDefending() && super.canContinueToUse();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.fox.clearStates();
            super.start();
        }
    }

    public static class BramblefoxGroupData extends AgeableMob.AgeableMobGroupData {
        public final Bramblefox.Type type;

        public BramblefoxGroupData(Bramblefox.Type p_28703_) {
            super(false);
            this.type = p_28703_;
        }
    }

    class BramblefoxLookAtPlayerGoal extends LookAtPlayerGoal {
        public BramblefoxLookAtPlayerGoal(Mob p_28707_, Class<? extends LivingEntity> p_28708_, float p_28709_) {
            super(p_28707_, p_28708_, p_28709_);
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return super.canUse() && !Bramblefox.this.isInterested();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !Bramblefox.this.isInterested();
        }
    }

    public class BramblefoxLookControl extends LookControl {
        public BramblefoxLookControl() {
            super(Bramblefox.this);
        }

        /**
         * Updates look
         */
        public void tick() {
            if (!Bramblefox.this.isSleeping()) {
                super.tick();
            }

        }

        protected boolean resetXRotOnTick() {
            return !Bramblefox.this.isPouncing() && !Bramblefox.this.isCrouching() && !Bramblefox.this.isInterested();
        }
    }

    class BramblefoxMoveControl extends MoveControl {
        public BramblefoxMoveControl() {
            super(Bramblefox.this);
        }

        public void tick() {
            if (Bramblefox.this.canMove()) {
                super.tick();
            }

        }
    }

    class BramblefoxPanicGoal extends PanicGoal {
        public BramblefoxPanicGoal(double p_28734_) {
            super(Bramblefox.this, p_28734_);
        }
        public void start() {
            Bramblefox.this.stageDecrease();
            this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
            this.isRunning = true;
        }
        public boolean shouldPanic() {
            return !Bramblefox.this.isDefending() && super.shouldPanic();
        }
    }



    class BramblefoxSearchForItemsGoal extends Goal {
        public BramblefoxSearchForItemsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if (!Bramblefox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            } else if (Bramblefox.this.getTarget() == null && Bramblefox.this.getLastHurtByMob() == null) {
                if (!Bramblefox.this.canMove()) {
                    return false;
                } else if (Bramblefox.this.getRandom().nextInt(reducedTickDelay(10)) != 0) {
                    return false;
                } else {
                    List<ItemEntity> list = Bramblefox.this.level().getEntitiesOfClass(ItemEntity.class, Bramblefox.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Bramblefox.ALLOWED_ITEMS);
                    return !list.isEmpty() && Bramblefox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
                }
            } else {
                return false;
            }
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            List<ItemEntity> list = Bramblefox.this.level().getEntitiesOfClass(ItemEntity.class, Bramblefox.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Bramblefox.ALLOWED_ITEMS);
            ItemStack itemstack = Bramblefox.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (itemstack.isEmpty() && !list.isEmpty()) {
                Bramblefox.this.getNavigation().moveTo(list.get(0), (double)1.2F);
            }

        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            List<ItemEntity> list = Bramblefox.this.level().getEntitiesOfClass(ItemEntity.class, Bramblefox.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Bramblefox.ALLOWED_ITEMS);
            if (!list.isEmpty()) {
                Bramblefox.this.getNavigation().moveTo(list.get(0), (double)1.2F);
            }

        }
    }



    class PerchAndSearchGoal extends Bramblefox.BramblefoxBehaviorGoal {
        private double relX;
        private double relZ;
        private int lookTime;
        private int looksRemaining;

        public PerchAndSearchGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return Bramblefox.this.getLastHurtByMob() == null && Bramblefox.this.getRandom().nextFloat() < 0.02F && !Bramblefox.this.isSleeping() && Bramblefox.this.getTarget() == null && Bramblefox.this.getNavigation().isDone() && !this.alertable() && !Bramblefox.this.isPouncing() && !Bramblefox.this.isCrouching();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return this.looksRemaining > 0;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.resetLook();
            this.looksRemaining = 2 + Bramblefox.this.getRandom().nextInt(3);
            Bramblefox.this.setSitting(true);
            Bramblefox.this.getNavigation().stop();
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            Bramblefox.this.setSitting(false);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            --this.lookTime;
            if (this.lookTime <= 0) {
                --this.looksRemaining;
                this.resetLook();
            }

            Bramblefox.this.getLookControl().setLookAt(Bramblefox.this.getX() + this.relX, Bramblefox.this.getEyeY(), Bramblefox.this.getZ() + this.relZ, (float)Bramblefox.this.getMaxHeadYRot(), (float)Bramblefox.this.getMaxHeadXRot());
        }

        private void resetLook() {
            double d0 = (Math.PI * 2D) * Bramblefox.this.getRandom().nextDouble();
            this.relX = Math.cos(d0);
            this.relZ = Math.sin(d0);
            this.lookTime = this.adjustedTickDelay(80 + Bramblefox.this.getRandom().nextInt(20));
        }
    }

    class SeekShelterGoal extends FleeSunGoal {
        private int interval = reducedTickDelay(100);

        public SeekShelterGoal(double p_28777_) {
            super(Bramblefox.this, p_28777_);
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if (!Bramblefox.this.isSleeping() && this.mob.getTarget() == null) {
                if (Bramblefox.this.level().isThundering()) {
                    return true;
                } else if (this.interval > 0) {
                    --this.interval;
                    return false;
                } else {
                    this.interval = 100;
                    BlockPos blockpos = this.mob.blockPosition();
                    return Bramblefox.this.level().isDay() && Bramblefox.this.level().canSeeSky(blockpos) && !((ServerLevel)Bramblefox.this.level()).isVillage(blockpos) && this.setWantedPos();
                }
            } else {
                return false;
            }
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            Bramblefox.this.clearStates();
            super.start();
        }
    }

    class SleepGoal extends Bramblefox.BramblefoxBehaviorGoal {
        private static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(140);
        private int countdown = Bramblefox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);

        public SleepGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if (Bramblefox.this.xxa == 0.0F && Bramblefox.this.yya == 0.0F && Bramblefox.this.zza == 0.0F) {
                return this.canSleep() || Bramblefox.this.isSleeping();
            } else {
                return false;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return this.canSleep();
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            } else {
                return Bramblefox.this.level().isDay() && this.hasShelter() && !this.alertable() && !Bramblefox.this.isInPowderSnow;
            }
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.countdown = Bramblefox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            Bramblefox.this.clearStates();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            Bramblefox.this.setSitting(false);
            Bramblefox.this.setIsCrouching(false);
            Bramblefox.this.setIsInterested(false);
            Bramblefox.this.setJumping(false);
            Bramblefox.this.setSleeping(true);
            Bramblefox.this.getNavigation().stop();
            Bramblefox.this.getMoveControl().setWantedPosition(Bramblefox.this.getX(), Bramblefox.this.getY(), Bramblefox.this.getZ(), 0.0D);
        }
    }

@Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return this.canPickUpLoot() && (pStack.getItem() != ModItems.BRAMBLEFOX_SCARF.get()) && (pStack.getItem() != ModItems.BRAMBLEFOX_BERRIES.get());
    }


    public static enum Type {
        STAGE0(0, "stage0"),
        STAGE1(1, "stage1"),
        STAGE2(2, "stage2"),
        STAGE3(3, "stage3");

        private static final Bramblefox.Type[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Bramblefox.Type::getId)).toArray(Type[]::new);
        private static final Map<String, Bramblefox.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Bramblefox.Type::getName, (p_28815_) -> p_28815_));
        private final int id;
        private final String name;

        private Type(int p_196658_, String p_196659_) {
            this.id = p_196658_;
            this.name = p_196659_;
        }

        public String getName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public static Bramblefox.Type byName(String pName) {
            return BY_NAME.getOrDefault(pName, STAGE3);
        }

        public static Bramblefox.Type byId(int pIndex) {
            if (pIndex < 0 || pIndex > BY_ID.length) {
                pIndex = 0;
            }

            return BY_ID[pIndex];
        }
    }
}


