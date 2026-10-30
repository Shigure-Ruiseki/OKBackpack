package ruiseki.backpack.backpack;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class AccessLogRecord {

    private final ResourceLocation backpackItemRegistryName;
    private final UUID backpackUuid;
    private final String playerName;
    private final String backpackName;
    private final int clothColor;
    private final int trimColor;
    private final long accessTime;
    private final int columnsTaken;

    public AccessLogRecord(ResourceLocation backpackItemRegistryName, UUID backpackUuid, String playerName,
        String backpackName, int clothColor, int trimColor, long accessTime, int columnsTaken) {
        this.backpackItemRegistryName = backpackItemRegistryName;
        this.backpackUuid = backpackUuid;
        this.playerName = playerName;
        this.backpackName = backpackName;
        this.clothColor = clothColor;
        this.trimColor = trimColor;
        this.accessTime = accessTime;
        this.columnsTaken = columnsTaken;
    }

    public UUID getBackpackUuid() {
        return backpackUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getBackpackName() {
        return backpackName;
    }

    public int getClothColor() {
        return clothColor;
    }

    public int getTrimColor() {
        return trimColor;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public int getColumnsTaken() {
        return columnsTaken;
    }

    public ResourceLocation getBackpackItemRegistryName() {
        return backpackItemRegistryName;
    }

    public NBTTagCompound serializeToNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setString("backpackItemRegistryName", backpackItemRegistryName.toString());
        ret.setString("backpackUuid", backpackUuid.toString());
        ret.setString("playerName", playerName);
        ret.setString("backpackName", backpackName);
        ret.setInteger("clothColor", clothColor);
        ret.setInteger("trimColor", trimColor);
        ret.setLong("accessTime", accessTime);
        ret.setInteger("columnsTaken", columnsTaken);
        return ret;
    }

    public static AccessLogRecord deserializeFromNBT(NBTTagCompound nbt) {
        return new AccessLogRecord(
            new ResourceLocation(nbt.getString("backpackItemRegistryName")),
            UUID.fromString(nbt.getString("backpackUuid")),
            nbt.getString("playerName"),
            nbt.getString("backpackName"),
            nbt.getInteger("clothColor"),
            nbt.getInteger("trimColor"),
            nbt.getLong("accessTime"),
            nbt.getInteger("columnsTaken"));
    }
}
