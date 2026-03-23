package ruiseki.okbackpack.common.entity.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class BackpackProperty implements IExtendedEntityProperties {

    private static final String PROPERTY_NAME = "okbackpack.property";
    private static final String TAG_STORED_SPAWN = "storedSpawn";

    private EntityPlayer player;
    private ChunkCoordinates storedSpawn = null;
    private boolean isWakingUpInDeployedBag = false;
    private boolean isSleepingInPortableBag = false;
    private boolean isWakingUpInPortableBag = false;

    public void setWakingUpInDeployedBag(boolean b) {
        this.isWakingUpInDeployedBag = b;
    }

    public boolean isWakingUpInDeployedBag() {
        return this.isWakingUpInDeployedBag;
    }

    public void setSleepingInPortableBag(boolean sleepingInPortableBag) {
        isSleepingInPortableBag = sleepingInPortableBag;
    }

    public boolean isSleepingInPortableBag() {
        return isSleepingInPortableBag;
    }

    public boolean isWakingUpInPortableBag() {
        return isWakingUpInPortableBag;
    }

    public void setWakingUpInPortableBag(boolean wakingUpInPortableBag) {
        isWakingUpInPortableBag = wakingUpInPortableBag;
    }

    public ChunkCoordinates getStoredSpawn() {
        return storedSpawn;
    }

    public BackpackProperty(EntityPlayer player) {
        this.player = player;
    }

    public NBTTagCompound getData() {
        NBTTagCompound data = new NBTTagCompound();
        saveNBTData(data);

        return data;
    }

    public static void register(EntityPlayer player) {
        player.registerExtendedProperties(PROPERTY_NAME, new BackpackProperty(player));
    }

    public static BackpackProperty get(EntityPlayer player) {
        return (BackpackProperty) player.getExtendedProperties(PROPERTY_NAME);
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        if (storedSpawn != null) {
            NBTTagCompound spawn = new NBTTagCompound();
            spawn.setInteger("posX", storedSpawn.posX);
            spawn.setInteger("posY", storedSpawn.posY);
            spawn.setInteger("posZ", storedSpawn.posZ);
            compound.setTag(TAG_STORED_SPAWN, spawn);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        if (compound != null) {
            if (compound.hasKey(TAG_STORED_SPAWN)) {
                NBTTagCompound spawn = compound.getCompoundTag(TAG_STORED_SPAWN);
                setStoredSpawn(
                    new ChunkCoordinates(spawn.getInteger("posX"), spawn.getInteger("posY"), spawn.getInteger("posZ")));
            }
        }
    }

    @Override
    public void init(Entity entity, World world) {
        this.player = (EntityPlayer) entity;
    }

    public void setStoredSpawn(ChunkCoordinates coords) {
        storedSpawn = coords;
    }
}
