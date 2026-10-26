package ruiseki.backpack.backpack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.persist.world.WorldStorage;

public class BackpackStorage extends WorldStorage {

    private final Map<UUID, NBTTagCompound> contents = new HashMap<>();
    private final Map<UUID, AccessLogRecord> accessLogRecords = new HashMap<>();

    private static BackpackStorage INSTANCE = null;

    public BackpackStorage(ModBase mod) {
        super(mod);
        this.setDirtyTrackingEnabled(true);
    }

    @Override
    public void reset() {
        this.contents.clear();
        this.accessLogRecords.clear();
    }

    @Override
    protected String getDataId() {
        return "storage";
    }

    public static BackpackStorage getInstance(ModBase modBase) {
        if (INSTANCE == null) {
            INSTANCE = new BackpackStorage(modBase);
        }
        return INSTANCE;
    }

    public static BackpackStorage get() {
        return getInstance(OKBackpack._instance);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        this.contents.clear();
        this.accessLogRecords.clear();

        NBTTagCompound contentsTag = tag.getCompoundTag("contents");
        for (String key : contentsTag.func_150296_c()) {
            this.contents.put(UUID.fromString(key), contentsTag.getCompoundTag(key));
        }

        NBTTagCompound accessLogTag = tag.getCompoundTag("accessLogRecords");
        for (String key : accessLogTag.func_150296_c()) {
            AccessLogRecord record = AccessLogRecord.deserializeFromNBT(accessLogTag.getCompoundTag(key));
            this.accessLogRecords.put(record.getBackpackUuid(), record);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagCompound contentsTag = new NBTTagCompound();
        for (Map.Entry<UUID, NBTTagCompound> entry : this.contents.entrySet()) {
            contentsTag.setTag(
                entry.getKey()
                    .toString(),
                entry.getValue());
        }
        tag.setTag("contents", contentsTag);

        NBTTagCompound accessLogTag = new NBTTagCompound();
        for (AccessLogRecord record : this.accessLogRecords.values()) {
            accessLogTag.setTag(
                record.getBackpackUuid()
                    .toString(),
                record.serializeToNBT());
        }
        tag.setTag("accessLogRecords", accessLogTag);
    }

    public NBTTagCompound getOrCreateBackpackContents(UUID uuid) {
        return contents.computeIfAbsent(uuid, uuid1 -> {
            this.markDirty();
            return new NBTTagCompound();
        });
    }

    public void putAccessLog(AccessLogRecord alr) {
        accessLogRecords.put(alr.getBackpackUuid(), alr);
        this.markDirty();
    }

    public void removeBackpackContents(UUID uuid) {
        contents.remove(uuid);
    }

    public void setContents(UUID uuid, NBTTagCompound contents) {
        if (!this.contents.containsKey(uuid)) {
            this.contents.put(uuid, contents);
        } else {
            NBTTagCompound currentContents = this.contents.get(uuid);
            for (String key : contents.func_150296_c()) {
                currentContents.setTag(key, contents.getTag(key));
            }
            markDirty();
        }
    }

    public Map<UUID, AccessLogRecord> getAccessLogs() {
        return accessLogRecords;
    }

    public int removeNonPlayerContents(boolean onlyWithEmptyInventory) {
        AtomicInteger numberRemoved = new AtomicInteger(0);
        this.contents.entrySet()
            .removeIf(entry -> {
                if (!onlyWithEmptyInventory || !entry.getValue()
                    .hasKey("inventory")) {
                    numberRemoved.incrementAndGet();
                    return true;
                }
                return false;
            });
        if (numberRemoved.get() > 0) {
            markDirty();
        }
        return numberRemoved.get();
    }

    private final Set<UUID> updatedBackpackSettingsFlags = new HashSet<>();

    public boolean removeUpdatedBackpackSettingsFlag(UUID backpackUuid) {
        return updatedBackpackSettingsFlags.remove(backpackUuid);
    }
}
