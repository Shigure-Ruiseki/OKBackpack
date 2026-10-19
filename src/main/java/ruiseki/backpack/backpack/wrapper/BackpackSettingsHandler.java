package ruiseki.backpack.backpack.wrapper;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.nbt.NBTTagCompound;

import ruiseki.backpack.settings.BackpackMainSettingsCategory;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.inventory.InventoryHandler;
import ruiseki.storagecore.settings.ISettingsCategory;
import ruiseki.storagecore.settings.SettingsHandler;

public class BackpackSettingsHandler extends SettingsHandler {

    public static final String SETTINGS_TAG = "settings";

    public BackpackSettingsHandler(IStorageWrapper backpackWrapper, NBTTagCompound backpackContentsNbt,
        Runnable markBackpackContentsDirty) {
        super(backpackContentsNbt, markBackpackContentsDirty, backpackWrapper::getInventoryHandler);
    }

    public void copyTo(SettingsHandler settingsHandler) {
        if (this.contentsNbt.hasKey(SETTINGS_TAG)) {
            // noinspection ConstantConditions - checking for whether tag exists just one line up
            settingsHandler.getNbt()
                .setTag(SETTINGS_TAG, this.contentsNbt.getTag(SETTINGS_TAG));
        }
    }

    @Override
    protected NBTTagCompound getSettingsNbtFromContentsNbt(NBTTagCompound contentsNbt) {
        return contentsNbt.getCompoundTag(SETTINGS_TAG);
    }

    @Override
    protected void addItemDisplayCategory(Supplier<InventoryHandler> inventoryHandlerSupplier,
        NBTTagCompound settingsNbt) {
        // addSettingsCategory(settingsNbt, ItemDisplaySettingsCategory.NAME, markContentsDirty, (categoryNbt, saveNbt)
        // ->
        // new ItemDisplaySettingsCategory(inventoryHandlerSupplier, categoryNbt, saveNbt, 1, () ->
        // getTypeCategory(MemorySettingsCategory.class)));
    }

    @Override
    protected void saveCategoryNbt(NBTTagCompound settingsNbt, String categoryName, NBTTagCompound tag) {
        settingsNbt.setTag(categoryName, tag);
        this.contentsNbt.setTag(SETTINGS_TAG, settingsNbt);
    }

    @Override
    public String getGlobalSettingsCategoryName() {
        return BackpackMainSettingsCategory.NAME;
    }

    @Override
    public ISettingsCategory instantiateGlobalSettingsCategory(NBTTagCompound categoryNbt,
        Consumer<NBTTagCompound> saveNbt) {
        return new BackpackMainSettingsCategory(categoryNbt, saveNbt);
    }

    @Override
    public BackpackMainSettingsCategory getGlobalSettingsCategory() {
        return getTypeCategory(BackpackMainSettingsCategory.class);
    }
}
