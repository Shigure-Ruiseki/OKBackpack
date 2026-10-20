package ruiseki.storagecore.settings.main;

import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;

public class StorageMainSettingCategory extends MainSettingsCategory<StorageMainSettingCategory> {

    public static final String OK_STORAGE_SETTINGS_PLAYER_TAG = "storageSettings";

    public static final String NAME = "storageGlobal";

    public StorageMainSettingCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
        super(categoryNbt, saveNbt, OK_STORAGE_SETTINGS_PLAYER_TAG);
    }
}
