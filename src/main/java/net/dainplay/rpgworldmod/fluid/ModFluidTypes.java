package net.dainplay.rpgworldmod.fluid;
import net.dainplay.rpgworldmod.RPGworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ModFluidTypes {
    public static final ResourceLocation ARBOR_FUEL_STILL_RL = new ResourceLocation(RPGworldMod.MOD_ID,"block/arbor_fuel_still");
    public static final ResourceLocation ARBOR_FUEL_FLOWING_RL = new ResourceLocation(RPGworldMod.MOD_ID,"block/arbor_fuel_flow");
    public static final ResourceLocation ARBOR_FUEL_OVERLAY_RL = new ResourceLocation(RPGworldMod.MOD_ID, "textures/misc/in_arbor_fuel.png");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, RPGworldMod.MOD_ID);

    public static final RegistryObject<FluidType> ARBOR_FUEL_FLUID_TYPE = register("arbor_fuel_fluid",
            FluidType.Properties.create()
                    .fallDistanceModifier(0F)
                    .density(3000)
                    .viscosity(1000)
                    .pathType(BlockPathTypes.LAVA)
                    .adjacentPathType(BlockPathTypes.DANGER_OTHER)
                    .supportsBoating(true)
                    .canConvertToSource(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA));



    private static RegistryObject<FluidType> register(String name, FluidType.Properties properties) {
        return FLUID_TYPES.register(name, () -> new BaseFluidType(ARBOR_FUEL_STILL_RL, ARBOR_FUEL_FLOWING_RL, ARBOR_FUEL_OVERLAY_RL,
                0xFFB18448, new Vector3f(177f / 255f, 132f / 255f, 72f / 255f), properties, ARBOR_FUEL_OVERLAY_RL)
        {
            @Override
            public double motionScale(Entity entity)
            {
                return 0.0023333333333333335D;
            }

            @Override
            public void setItemMovement(ItemEntity entity)
            {
                Vec3 vec3 = entity.getDeltaMovement();
                entity.setDeltaMovement(vec3.x * (double)0.95F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.95F);
            }
        });
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}