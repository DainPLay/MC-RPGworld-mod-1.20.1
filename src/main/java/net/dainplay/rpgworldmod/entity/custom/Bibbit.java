package net.dainplay.rpgworldmod.entity.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.dainplay.rpgworldmod.data.tags.ModAdvancements;
import net.dainplay.rpgworldmod.effect.ModEffects;
import net.dainplay.rpgworldmod.effect.ParalysisEffect;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.dainplay.rpgworldmod.sounds.RPGSounds;
import net.dainplay.rpgworldmod.world.RPGLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class Bibbit extends PathfinderMob {

    protected static final ImmutableList<SensorType<? extends Sensor<? super Bibbit>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bibbit.class, EntityDataSerializers.BYTE);
    private static final Predicate<Entity> AVOID_PLAYERS = (player) ->
            ((ModItems.FIG_LEAF.get().isEquippedBy((LivingEntity) player))
            && checkVisibility(((LivingEntity) player), ModItems.FIG_LEAF.get()))
            || ((Player)player).getMainHandItem().getItem() == ModItems.FIG_LEAF.get()
            || ((Player)player).getOffhandItem().getItem() == ModItems.FIG_LEAF.get();

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

    public Bibbit(EntityType<? extends Bibbit> pEntityType, Level p_28452_) {
        super(pEntityType, p_28452_);
        this.lookControl = new BibbitLookControl();
        this.moveControl = new BibbitMoveControl();
        this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0F);
        this.setCanPickUpLoot(true);
    }
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }


    protected static void updateActivity(Bibbit bibbit) {
        Brain<Bibbit> brain = bibbit.getBrain();
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BibbitFloatGoal());
        this.goalSelector.addGoal(1, new BibbitPanicGoal(this,200));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.6D, 1.4D, AVOID_PLAYERS::test));
        this.goalSelector.addGoal(4, new BibbitTemptGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new BibbitTemptBibbitGoal(this, 1.2D, false));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(12, new BibbitLookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(13, new Bibbit.PerchAndSearchGoal());
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    protected void customServerAiStep() {
        this.level().getProfiler().push("bibbitBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        updateActivity(this);
        super.customServerAiStep();
    }
    public void aiStep() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {

            LivingEntity livingentity = this.getTarget();
            if (livingentity == null || !livingentity.isAlive()) {
                this.setIsInterested(false);
            }
        }

        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }


        super.aiStep();

    }
    public boolean isPushable() {
        return !this.isCrouching();
    }
    /**
     * Dead and sleeping entities cannot move
     */
    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.FOLLOW_RANGE, 32.0D);
    }
    public static boolean checkBibbitSpawnRules(EntityType<Bibbit> p_186214_, LevelAccessor p_186215_, MobSpawnType p_186216_, BlockPos p_186217_, RandomSource p_186218_) {
        return p_186215_.getBlockState(p_186217_.below()).is(BlockTags.FOXES_SPAWNABLE_ON) && isBrightEnoughToSpawn(p_186215_, p_186217_);
    }

    protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter pLevel, BlockPos pPos) {
        return pLevel.getRawBrightness(pPos, 0) > 8;
    }
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.75F;
    }



    protected Brain.Provider<Bibbit> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<Bibbit> brain = this.brainProvider().makeBrain(pDynamic);
        initIdleActivity(brain);
        initCoreActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initIdleActivity(Brain<Bibbit> pBrain) {
        pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(createIdleLookBehaviors(), createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4), GoToWantedItem.create(1.0F, true, 9)));
    }
    private static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<>(ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder().add(Pair.of(new DoNothing(30, 60), 1)).build());
    }

    private static RunOne<Bibbit> createIdleMovementBehaviors() {
        return new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(ModEntities.BIBBIT.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(new DoNothing(30, 60), 1)));
    }
    public Brain<Bibbit> getBrain() {
        return (Brain<Bibbit>)super.getBrain();
    }
    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);

        pCompound.putBoolean("Crouching", this.isCrouching());
        pCompound.putBoolean("Was crouching", this.isCrouching());
    }
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);

        this.setIsCrouching(pCompound.getBoolean("Crouching"));
        was_crouching = pCompound.getBoolean("Was crouching");

    }
    public static boolean was_crouching = false;
    static class BibbitPanicGoal extends Goal {

        private static int timer;
        private final Bibbit mob;
        private final int duration;  // in ticks
        private static final UUID INVULNERABILITY_MODIFIER_UUID = UUID.fromString("d3064e68-58ff-4826-a2f9-cb8880749c6a");
        private static final String INVULNERABILITY_MODIFIER_NAME = "Custom Panic Modifier";

        public BibbitPanicGoal(Bibbit mob, int durationTicks) {
            this.mob = mob;
            this.duration = durationTicks;
            this.setFlags(EnumSet.of(Flag.MOVE)); // Important: This goal needs to be able to modify the entities movement
        }

        @Override
        public boolean canUse() {
            if (this.mob.isInWater() && this.mob.getFluidHeight(FluidTags.WATER) > 0.25D || this.mob.isInLava() || this.mob.isInFluidType()
                    || !this.mob.level().getBlockState(this.mob.getOnPos(1)).getFluidState().isEmpty()) return false;
            return this.mob.getLastHurtByMob() != null || this.mob.getLastAttacker() != null || was_crouching;
        }

        @Override
        public boolean canContinueToUse() {

            if (this.mob.isInWater() && this.mob.getFluidHeight(FluidTags.WATER) > 0.25D || this.mob.isInLava() || this.mob.isInFluidType()) {
                timer = duration;
            }

            return timer < duration;
        }

        @Override
        public void start() {
            timer = 0;
            applyInvulnerability();
            mob.setIsCrouching(true);
            was_crouching = false;
            this.mob.setInvulnerable(true);
            this.mob.getNavigation().stop();
            this.mob.setSpeed(0F);
        }

        @Override
        public void tick() {
            if(timer < duration){
                mob.heal(8); // Heal every tick
                timer++;
            }
        }

        @Override
        public void stop() {
            removeInvulnerability();
            mob.setIsCrouching(false);
            timer = 0;
            was_crouching = false;
            this.mob.setInvulnerable(false);
            this.mob.setSpeed((float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        }

        private void applyInvulnerability() {
            AttributeInstance attribute = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (attribute != null) {
                AttributeModifier modifier = new AttributeModifier(INVULNERABILITY_MODIFIER_UUID, INVULNERABILITY_MODIFIER_NAME, 1.0, AttributeModifier.Operation.ADDITION);
                attribute.addTransientModifier(modifier);
            }
            AttributeInstance damageAttribute = mob.getAttribute(Attributes.ARMOR);
            if (damageAttribute != null) {
                AttributeModifier modifier = new AttributeModifier(INVULNERABILITY_MODIFIER_UUID, INVULNERABILITY_MODIFIER_NAME, 20, AttributeModifier.Operation.ADDITION); // a lot of armor
                damageAttribute.addTransientModifier(modifier);
            }
        }

        private void removeInvulnerability() {
            AttributeInstance attribute = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if(attribute != null)
                attribute.removeModifier(INVULNERABILITY_MODIFIER_UUID);
            AttributeInstance damageAttribute = mob.getAttribute(Attributes.ARMOR);
            if (damageAttribute != null) {
                damageAttribute.removeModifier(INVULNERABILITY_MODIFIER_UUID);
            }
        }
    }

    public boolean wantsToPickUp(ItemStack pStack) {
        return this.canPickUpLoot() && pStack.is(Tags.Items.ARMORS_BOOTS) && this.getItemBySlot(EquipmentSlot.FEET).isEmpty();
    }

    /**
     * Returns whether player is sleeping or not
     */

    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        return pPotioneffect.getEffect() == ModEffects.MOSSIOSIS.get() ? false : super.canBeAffected(pPotioneffect);
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
        EquipmentSlot equipmentslot = this.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(equipmentslot).isEmpty()) {
            return false;
        } else {
            return equipmentslot == EquipmentSlot.FEET && super.canTakeItem(pItemstack);
        }
    }

    public static EquipmentSlot getEquipmentSlotForItem(ItemStack pItem) {
        final EquipmentSlot slot = pItem.getEquipmentSlot();
        if (slot != null) return slot; // FORGE: Allow modders to set a non-default equipment slot for a stack; e.g. a non-armor chestplate-slot item
        Equipable equipable = Equipable.get(pItem);
        return equipable != null ? equipable.getEquipmentSlot() : EquipmentSlot.FEET;
    }

    public boolean canHoldItem(ItemStack pStack) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.FEET);
        return itemstack.isEmpty();
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
        Item boots = pItemEntity.getItem().getItem();
        if(this.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ParalysisEffect.MODIFIER_UUID) != null
        && !this.getItemBySlot(EquipmentSlot.FEET).isEmpty())
            return;
        ItemStack itemstack = pItemEntity.getItem();
        if (this.canHoldItem(itemstack)) {
            int i = itemstack.getCount();
            if (i > 1) {
                this.dropItemStack(itemstack.split(i - 1));
            }

            this.onItemPickup(pItemEntity);
            this.setItemSlot(EquipmentSlot.FEET, itemstack.split(1));
            this.handDropChances[EquipmentSlot.FEET.getIndex()] = 2.0F;
            this.take(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
            stopWalking(this);
            Optional<Player> optional = this.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
            if (optional.isPresent()) {
                throwItemsTowardPlayer(this, optional.get(), getBarterResponseItems(this, boots));
            } else {
                throwItemsTowardRandomPos(this, getBarterResponseItems(this, boots));
            }
        }

    }
    @Override
    public boolean isPersistenceRequired() {
        return true;
    }
    private static List<ItemStack> getBarterResponseItems(Bibbit bibbit, Item item) {
        LootTable loottable = bibbit.level().getServer().getLootData().getLootTable(RPGLootTables.BIBBIT1);
        if(item instanceof ArmorItem boots && boots.getDefense() > 1) loottable = bibbit.level().getServer().getLootData().getLootTable(RPGLootTables.BIBBIT2);
        if(item instanceof ArmorItem boots && boots.getDefense() > 2) {
            loottable = bibbit.level().getServer().getLootData().getLootTable(RPGLootTables.BIBBIT3);
            for(ServerPlayer serverplayer : bibbit.level().getEntitiesOfClass(ServerPlayer.class, new AABB(bibbit.blockPosition()).inflate(10.0D, 5.0D, 10.0D))) {
                ModAdvancements.SHOE_A_BIBBIT.trigger(serverplayer);
            }
        }
        List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel)bibbit.level())).withParameter(LootContextParams.THIS_ENTITY, bibbit).create(LootContextParamSets.PIGLIN_BARTER));
        return list;
    }
    private static Vec3 getRandomNearbyPos(Bibbit bibbit) {
        Vec3 vec3 = LandRandomPos.getPos(bibbit, 4, 2);
        return vec3 == null ? bibbit.position() : vec3;
    }
    private static void throwItemsTowardRandomPos(Bibbit bibbit, List<ItemStack> pStacks) {
        throwItemsTowardPos(bibbit, pStacks, getRandomNearbyPos(bibbit));
    }

    private static void throwItemsTowardPlayer(Bibbit bibbit, Player pPlayer, List<ItemStack> pStacks) {
        double d2 = pPlayer.getX() - bibbit.getX();
        double d1 = pPlayer.getZ() - bibbit.getZ();
        bibbit.setYRot(-((float)Mth.atan2(d2, d1)) * (180F / (float)Math.PI));
        bibbit.yBodyRot = bibbit.getYRot();
        throwItemsTowardPos(bibbit, pStacks, pPlayer.position());
    }

    private static void initCoreActivity(Brain<Bibbit> pBrain) {
        pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }
    private static void throwItemsTowardPos(Bibbit bibbit, List<ItemStack> pStacks, Vec3 pPos) {
        if (!pStacks.isEmpty()) {
            bibbit.jumpFromGround();
            for(ItemStack itemstack : pStacks) {
                BehaviorUtils.throwItem(bibbit, itemstack, pPos.add(0.0D, 1.0D, 0.0D));
            }
        }

    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();

        this.interestedAngleO = this.interestedAngle;
        if (this.isInterested()) {
            this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
        } else {
            this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
        }

        this.crouchAmountO = this.crouchAmount;

    }
    private static void stopWalking(Bibbit bibbit) {
        bibbit.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        bibbit.getNavigation().stop();
    }
    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */


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




    void clearStates() {
        this.setIsInterested(false);
        this.setIsCrouching(false);
    }

    boolean canMove() {
        return !this.isCrouching();
    }


    @Nullable
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        if(this.isCrouching()) return RPGSounds.SITTING_BIBBIT_HURT.get(); else return RPGSounds.BIBBIT_HURT.get();
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return RPGSounds.BIBBIT_DEATH.get();
    }


    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        super.dropAllDeathLoot(pDamageSource);
    }
    protected void dropEquipment() { // Forge: move extra drops to dropEquipment to allow them to be captured by LivingDropsEvent
        super.dropEquipment();
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.FEET);
        if (!itemstack.isEmpty()) {
            this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
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

    public class BramblefoxAlertableEntitiesSelector implements Predicate<LivingEntity> {
        public boolean test(LivingEntity p_28653_) {
            if (p_28653_ instanceof Bibbit) {
                return false;
            } else {
                        return !p_28653_.isSleeping() && !p_28653_.isDiscrete();
                    }
        }
    }

    abstract class BibbitBehaviorGoal extends Goal {
        private final TargetingConditions alertableTargeting = TargetingConditions.forCombat().range(12.0D).ignoreLineOfSight().selector(Bibbit.this.new BramblefoxAlertableEntitiesSelector());

        protected boolean alertable() {
            return !Bibbit.this.level().getNearbyEntities(LivingEntity.class, this.alertableTargeting, Bibbit.this, Bibbit.this.getBoundingBox().inflate(12.0D, 6.0D, 12.0D)).isEmpty();
        }
    }

    class BibbitFloatGoal extends FloatGoal {
        public BibbitFloatGoal() {
            super(Bibbit.this);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            super.start();
            Bibbit.this.clearStates();
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return Bibbit.this.isInWater() && Bibbit.this.getFluidHeight(FluidTags.WATER) > 0.25D || Bibbit.this.isInLava() || Bibbit.this.isInFluidType();
        }
    }


    class BibbitLookAtPlayerGoal extends LookAtPlayerGoal {
        public BibbitLookAtPlayerGoal(Mob p_28707_, Class<? extends LivingEntity> p_28708_, float p_28709_) {
            super(p_28707_, p_28708_, p_28709_);
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return super.canUse() && !Bibbit.this.isInterested() && !this.mob.isCrouching();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !Bibbit.this.isInterested();
        }
    }

    public class BibbitLookControl extends LookControl {
        public BibbitLookControl() {
            super(Bibbit.this);
        }

        /**
         * Updates look
         */
        public void tick() {
                super.tick();

        }

        protected boolean resetXRotOnTick() {
            return !Bibbit.this.isPouncing() && !Bibbit.this.isCrouching() && !Bibbit.this.isInterested();
        }
    }

    class BibbitMoveControl extends MoveControl {
        public BibbitMoveControl() {
            super(Bibbit.this);
        }

        public void tick() {
            if (Bibbit.this.canMove()) {
                super.tick();
            }

        }
    }





    class PerchAndSearchGoal extends BibbitBehaviorGoal {
        private double relX;
        private double relZ;
        private int lookTime;
        private int looksRemaining;

        public PerchAndSearchGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return Bibbit.this.getLastHurtByMob() == null && Bibbit.this.getRandom().nextFloat() < 0.02F && !Bibbit.this.isSleeping() && Bibbit.this.getTarget() == null && Bibbit.this.getNavigation().isDone() && !this.alertable() && !Bibbit.this.isPouncing() && !Bibbit.this.isCrouching();
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
            this.looksRemaining = 2 + Bibbit.this.getRandom().nextInt(3);
            Bibbit.this.getNavigation().stop();
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

            Bibbit.this.getLookControl().setLookAt(Bibbit.this.getX() + this.relX, Bibbit.this.getEyeY(), Bibbit.this.getZ() + this.relZ, (float) Bibbit.this.getMaxHeadYRot(), (float) Bibbit.this.getMaxHeadXRot());
        }

        private void resetLook() {
            double d0 = (Math.PI * 2D) * Bibbit.this.getRandom().nextDouble();
            this.relX = Math.cos(d0);
            this.relZ = Math.sin(d0);
            this.lookTime = this.adjustedTickDelay(80 + Bibbit.this.getRandom().nextInt(20));
        }
    }


}


