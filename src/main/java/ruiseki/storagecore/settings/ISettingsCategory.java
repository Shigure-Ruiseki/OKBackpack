package ruiseki.storagecore.settings;

import net.minecraft.nbt.NBTTagCompound;

public interface ISettingsCategory<T extends ISettingsCategory<?>> {

    void reloadFrom(NBTTagCompound categoryNbt);

    void overwriteWith(T otherCategory);

    boolean isLargerThanNumberOfSlots(int slots);
}
