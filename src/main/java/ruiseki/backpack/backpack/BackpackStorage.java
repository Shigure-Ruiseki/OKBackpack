package ruiseki.backpack.backpack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.persist.world.WorldStorage;

public class BackpackStorage extends WorldStorage {

    private final Map<UUID, NBTTagCompound> contents = new HashMap<>();

    private static BackpackStorage INSTANCE = null;

    public BackpackStorage(ModBase mod) {
        super(mod);
        this.setDirtyTrackingEnabled(true);
    }

    @Override
    public void reset() {
        contents.clear();
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
        NBTTagCompound entries = tag.getCompoundTag("contents");
        for (String key : entries.func_150296_c()) {
            UUID uuid = UUID.fromString(key);
            contents.put(uuid, entries.getCompoundTag(key));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound entries = new NBTTagCompound();
        for (Map.Entry<UUID, NBTTagCompound> entry : contents.entrySet()) {
            entries.setTag(
                entry.getKey()
                    .toString(),
                entry.getValue());
        }
        tag.setTag("contents", entries);
        super.writeToNBT(tag);
    }

    public NBTTagCompound getOrCreateBackpackContents(UUID uuid) {
        return contents.computeIfAbsent(uuid, uuid1 -> {
            this.markDirty();
            return new NBTTagCompound();
        });
    }

    public void removeContents(UUID uuid) {
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
}
