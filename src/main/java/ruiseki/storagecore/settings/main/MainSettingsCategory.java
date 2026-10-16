package ruiseki.storagecore.settings.main;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;

import ruiseki.storagecore.settings.ISettingsCategory;
import ruiseki.storagecore.settings.MainSetting;

public class MainSettingsCategory<T extends MainSettingsCategory<?>> implements ISettingsCategory<T> {

    public static final String NAME = "global";
    private NBTTagCompound categoryNbt;
    private final Consumer<NBTTagCompound> saveNbt;

    private final String playerSettingsTagName;

    public MainSettingsCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt,
        String playerSettingsTagName) {
        this.categoryNbt = categoryNbt;
        this.saveNbt = saveNbt;
        this.playerSettingsTagName = playerSettingsTagName;
    }

    public String getPlayerSettingsTagName() {
        return playerSettingsTagName;
    }

    public <S> Optional<S> getSettingValue(MainSetting<S> setting) {
        return setting.getValue(categoryNbt);
    }

    public <S> void setSettingValue(MainSetting<S> setting, S value) {
        setting.setValue(categoryNbt, value);
        saveNbt.accept(categoryNbt);
    }

    public <S> void removeSetting(MainSetting<S> setting) {
        setting.removeFrom(categoryNbt);
        saveNbt.accept(categoryNbt);
    }

    @Override
    public void reloadFrom(NBTTagCompound categoryNbt) {
        this.categoryNbt = categoryNbt;
    }

    @Override
    public void overwriteWith(T otherCategory) {
        // noop for now
    }

    @Override
    public boolean isLargerThanNumberOfSlots(int slots) {
        return false; // no slots in this category so it can't be too large
    }
}
