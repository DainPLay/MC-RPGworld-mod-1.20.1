package net.dainplay.rpgworldmod.entity.custom;

import net.dainplay.rpgworldmod.block.ModBlocks;
import net.dainplay.rpgworldmod.entity.ModEntities;
import net.dainplay.rpgworldmod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkHooks;

public class ModBoat extends Boat {

    private static final EntityDataAccessor<Integer> BOAT_TYPE = SynchedEntityData.defineId(ModBoat.class, EntityDataSerializers.INT);

    public ModBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public ModBoat(Level level, double x, double y, double z) {
        this(ModEntities.MODBOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public ModBoat.Type getModBoatType() {
        return ModBoat.Type.byId(this.getEntityData().get(BOAT_TYPE));
    }

    @Override
    public Item getDropItem() {
        return switch (this.getModBoatType()) {
            case RIE -> ModItems.RIE_BOAT.get();
        };
    }

    public void setModBoatType(ModBoat.Type boatType) {
        this.getEntityData().set(BOAT_TYPE, boatType.ordinal());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(BOAT_TYPE, Type.RIE.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Type", this.getModBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Type", 8)) {
            this.setModBoatType(ModBoat.Type.getTypeFromString(tag.getString("Type")));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public enum Type {
        RIE(ModBlocks.RIE_PLANKS.get(), "rie");

        private final String name;
        private final Block block;

        Type(Block block, String name) {
            this.name = name;
            this.block = block;
        }

        public String getName() {
            return this.name;
        }

        public Block asPlank() {
            return this.block;
        }

        public String toString() {
            return this.name;
        }

        public static ModBoat.Type byId(int id) {
            ModBoat.Type[] types = values();
            if (id < 0 || id >= types.length) {
                id = 0;
            }

            return types[id];
        }

        public static ModBoat.Type getTypeFromString(String nameIn) {
            ModBoat.Type[] types = values();

            for (Type type : types) {
                if (type.getName().equals(nameIn)) {
                    return type;
                }
            }

            return types[0];
        }
    }
}