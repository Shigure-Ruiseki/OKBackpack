package ruiseki.okbackpack.common.entity.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;

public class BackpackProperty implements IExtendedEntityProperties {

    private static final String PROPERTY_NAME = "okbackpack.property";
    private static final String TAG_STORED_SPAWN = "storedSpawn";
    private static final String TAG_KEEP_TAB = "keepTab";
    private static final String TAG_SHIFT_CLICK_INTO_OPEN_TAB = "shiftClickIntoOpenTab";
    private static final String TAG_KEEP_SEARCH_PHRASE = "keepSearchPhrase";
    private static final String TAG_LOCK_BACKPACK = "lockBackpack";

    private EntityPlayer player;
    private ChunkCoordinates storedSpawn = null;
    private boolean isWakingUpInDeployedBag = false;
    private boolean isSleepingInPortableBag = false;
    private boolean isWakingUpInPortableBag = false;
    private boolean keepTab = true;
    private boolean shiftClickIntoOpenTab = false;
    private boolean keepSearchPhrase = false;
    private boolean lockBackpack = false;

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

    public boolean isKeepTab() {
        return keepTab;
    }

    public void setKeepTab(boolean keepTab) {
        this.keepTab = keepTab;
    }

    public boolean isShiftClickIntoOpenTab() {
        return shiftClickIntoOpenTab;
    }

    public void setShiftClickIntoOpenTab(boolean shiftClickIntoOpenTab) {
        this.shiftClickIntoOpenTab = shiftClickIntoOpenTab;
    }

    public boolean isKeepSearchPhrase() {
        return keepSearchPhrase;
    }

    public void setKeepSearchPhrase(boolean keepSearchPhrase) {
        this.keepSearchPhrase = keepSearchPhrase;
    }

    public boolean isLockBackpack() {
        return lockBackpack;
    }

    public void setLockBackpack(boolean lockBackpack) {
        this.lockBackpack = lockBackpack;
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

        compound.setBoolean(TAG_KEEP_TAB, keepTab);
        compound.setBoolean(TAG_SHIFT_CLICK_INTO_OPEN_TAB, shiftClickIntoOpenTab);
        compound.setBoolean(TAG_KEEP_SEARCH_PHRASE, keepSearchPhrase);
        compound.setBoolean(TAG_LOCK_BACKPACK, lockBackpack);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        if (compound != null) {
            if (compound.hasKey(TAG_STORED_SPAWN)) {
                NBTTagCompound spawn = compound.getCompoundTag(TAG_STORED_SPAWN);
                setStoredSpawn(
                    new ChunkCoordinates(spawn.getInteger("posX"), spawn.getInteger("posY"), spawn.getInteger("posZ")));
            }

            keepTab = !compound.hasKey(TAG_KEEP_TAB) || compound.getBoolean(TAG_KEEP_TAB);
            shiftClickIntoOpenTab = compound.getBoolean(TAG_SHIFT_CLICK_INTO_OPEN_TAB);
            keepSearchPhrase = compound.getBoolean(TAG_KEEP_SEARCH_PHRASE);
            lockBackpack = compound.getBoolean(TAG_LOCK_BACKPACK);
        }
    }

    @Override
    public void init(Entity entity, World world) {
        this.player = (EntityPlayer) entity;
    }

    public void setStoredSpawn(ChunkCoordinates coords) {
        storedSpawn = coords;
    }

    public void applySettingsToWrapper(IStorageWrapper wrapper) {
        wrapper.setKeepTab(keepTab);
        wrapper.setShiftClickIntoOpenTab(shiftClickIntoOpenTab);
        wrapper.setKeepSearchPhrase(keepSearchPhrase);
        wrapper.setLockStorage(lockBackpack);
    }

    public void syncGlobalSettingsToOwnedBackpacks() {
        if (player == null || player.worldObj == null || player.worldObj.isRemote) {
            return;
        }

        BackpackEntityHelper
            .visitPlayerBackpacks(player, BackpackEntityHelper.SearchOrder.PLAYER_THEN_BAUBLES, context -> {
                BackpackWrapper wrapper = context.wrapper();
                if (!wrapper.isUsePlayerSettings()) {
                    return false;
                }

                applySettingsToWrapper(wrapper);
                wrapper.setPlayerUUID(
                    player.getUniqueID()
                        .toString());
                BackpackEntityHelper.persistBackpack(context);
                return false;
            });
    }
}
