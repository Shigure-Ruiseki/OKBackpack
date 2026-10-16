package ruiseki.storagecore.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.nbt.NBTTagCompound;

import ruiseki.storagecore.inventory.InventoryHandler;
import ruiseki.storagecore.settings.main.MainSettingsCategory;
import ruiseki.storagecore.settings.memory.MemorySettingsCategory;
import ruiseki.storagecore.settings.nosort.NoSortSettingsCategory;

public abstract class SettingsHandler {

    protected NBTTagCompound contentsNbt;
    protected final Runnable markContentsDirty;
    protected final Map<String, ISettingsCategory<?>> settingsCategories = new LinkedHashMap<>();
    private final Map<Class<?>, List<?>> interfaceCategories = new HashMap<>();
    private final Map<Class<? extends ISettingsCategory<?>>, ISettingsCategory<?>> typeCategories = new HashMap<>();

    protected SettingsHandler(NBTTagCompound contentsNbt, Runnable markContentsDirty,
        Supplier<InventoryHandler> inventoryHandlerSupplier) {
        this.contentsNbt = contentsNbt;
        this.markContentsDirty = markContentsDirty;
        addSettingsCategories(inventoryHandlerSupplier, getSettingsNbtFromContentsNbt(contentsNbt));
    }

    protected abstract NBTTagCompound getSettingsNbtFromContentsNbt(NBTTagCompound contentsNbt);

    private void addSettingsCategories(Supplier<InventoryHandler> inventoryHandlerSupplier,
        NBTTagCompound settingsNbt) {
        addSettingsCategory(
            settingsNbt,
            getGlobalSettingsCategoryName(),
            markContentsDirty,
            this::instantiateGlobalSettingsCategory);
        addSettingsCategory(settingsNbt, NoSortSettingsCategory.NAME, markContentsDirty, NoSortSettingsCategory::new);
        addSettingsCategory(
            settingsNbt,
            MemorySettingsCategory.NAME,
            markContentsDirty,
            (categoryNbt, saveNbt) -> new MemorySettingsCategory(inventoryHandlerSupplier, categoryNbt, saveNbt));
        addItemDisplayCategory(inventoryHandlerSupplier, settingsNbt);
    }

    protected abstract void addItemDisplayCategory(Supplier<InventoryHandler> inventoryHandlerSupplier,
        NBTTagCompound settingsNbt);

    public abstract String getGlobalSettingsCategoryName();

    public abstract ISettingsCategory<?> instantiateGlobalSettingsCategory(NBTTagCompound categoryNbt,
        Consumer<NBTTagCompound> saveNbt);

    public MainSettingsCategory<?> getGlobalSettingsCategory() {
        return getTypeCategory(MainSettingsCategory.class);
    }

    protected void addSettingsCategory(NBTTagCompound settingsNbt, String categoryName, Runnable markContentsDirty,
        BiFunction<NBTTagCompound, Consumer<NBTTagCompound>, ISettingsCategory<?>> instantiateCategory) {
        ISettingsCategory<?> category = instantiateCategory.apply(settingsNbt.getCompoundTag(categoryName), tag -> {
            saveCategoryNbt(settingsNbt, categoryName, tag);
            markContentsDirty.run();
        });
        settingsCategories.put(categoryName, category);
        addTypeCategory(category);
    }

    private <T extends ISettingsCategory<T>> void addTypeCategory(ISettingsCategory<?> category) {
        // noinspection unchecked
        typeCategories.put((Class<T>) category.getClass(), category);
    }

    protected abstract void saveCategoryNbt(NBTTagCompound settingsNbt, String categoryName, NBTTagCompound tag);

    public Map<String, ISettingsCategory<?>> getSettingsCategories() {
        return settingsCategories;
    }

    public <T> List<T> getCategoriesThatImplement(Class<T> categoryClass) {
        // noinspection unchecked
        return (List<T>) interfaceCategories.computeIfAbsent(categoryClass, this::getListOfWrappersThatImplement);
    }

    public <T extends ISettingsCategory<?>> T getTypeCategory(Class<T> categoryClazz) {
        // noinspection unchecked - only inserted in one place where it's made sure that class is the same as the
        // category instance
        return (T) typeCategories.get(categoryClazz);
    }

    private <T> List<T> getListOfWrappersThatImplement(Class<T> uc) {
        List<T> ret = new ArrayList<>();
        for (ISettingsCategory<?> category : settingsCategories.values()) {
            if (uc.isInstance(category)) {
                // noinspection unchecked
                ret.add((T) category);
            }
        }
        return ret;
    }

    public NBTTagCompound getNbt() {
        return getSettingsNbtFromContentsNbt(contentsNbt);
    }

    public void reloadFrom(NBTTagCompound contentsNbt) {
        NBTTagCompound settingsNbt = getSettingsNbtFromContentsNbt(contentsNbt);
        getSettingsCategories()
            .forEach((categoryName, category) -> category.reloadFrom(settingsNbt.getCompoundTag(categoryName)));
    }
}
