package ruiseki.storagecore.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.storagecore.settings.main.MainSettingsCategory;

public class SettingsManager {

    private SettingsManager() {}

    private static final Map<String, MainSetting<?>> settings = new HashMap<>();

    public static final MainSetting<Boolean> SHIFT_CLICK_INTO_OPEN_TAB_FIRST = new MainSetting<>(
        "shiftClickOpenTab",
        NBTHelpers::getBoolean,
        NBTTagCompound::setBoolean,
        true);
    public static final MainSetting<Boolean> KEEP_TAB_OPEN = new MainSetting<>(
        "keepTabOpen",
        NBTHelpers::getBoolean,
        NBTTagCompound::setBoolean,
        true);

    static {
        settings.put(SHIFT_CLICK_INTO_OPEN_TAB_FIRST.getName(), SHIFT_CLICK_INTO_OPEN_TAB_FIRST);
        settings.put(KEEP_TAB_OPEN.getName(), KEEP_TAB_OPEN);
    }

    public static void addSetting(MainSetting<?> setting) {
        settings.put(setting.getName(), setting);
    }

    public static Optional<MainSetting<?>> getSetting(String settingName) {
        return Optional.ofNullable(settings.get(settingName));
    }

    public static <T> T getPlayerSettingOrDefault(EntityPlayer player, String playerSettingsTagName,
        MainSetting<T> setting) {
        return getPlayerSetting(player, playerSettingsTagName, setting).orElse(setting.getDefaultValue());
    }

    public static <T> Optional<T> getPlayerSetting(EntityPlayer player, String playerSettingsTagName,
        MainSetting<T> setting) {
        return setting.getValue(getPlayerSettingsTag(player, playerSettingsTagName));
    }

    public static NBTTagCompound getPlayerSettingsTag(EntityPlayer player, String playerSettingsTagName) {
        return player.getEntityData()
            .getCompoundTag(playerSettingsTagName);
    }

    public static void setPlayerSettingsTag(EntityPlayer player, String playerSettingsTagName,
        NBTTagCompound settingsNbt) {
        player.getEntityData()
            .setTag(playerSettingsTagName, settingsNbt);
    }

    public static <T> void setPlayerSetting(EntityPlayer player, String playerSettingsTagName, MainSetting<T> setting,
        T value) {
        if (!player.getEntityData()
            .hasKey(playerSettingsTagName)) {
            player.getEntityData()
                .setTag(playerSettingsTagName, new NBTTagCompound());
        }
        if (value != setting.getDefaultValue()) {
            setting.setValue(getPlayerSettingsTag(player, playerSettingsTagName), value);
        } else {
            setting.removeFrom(getPlayerSettingsTag(player, playerSettingsTagName));
        }
    }

    public static <T> void setSetting(EntityPlayer player, String playerSettingsTagName,
        MainSettingsCategory<?> category, MainSetting<T> setting, T value) {
        T playerSettingValue = getPlayerSetting(player, playerSettingsTagName, setting)
            .orElse(setting.getDefaultValue());
        if (playerSettingValue != value) {
            category.setSettingValue(setting, value);
        } else {
            category.removeSetting(setting);
        }
    }

    public static <T> T getSettingValue(EntityPlayer player, String playerSettingsTagName,
        MainSettingsCategory<?> category, MainSetting<T> setting) {
        return category.getSettingValue(setting)
            .orElse(getPlayerSetting(player, playerSettingsTagName, setting).orElse(setting.getDefaultValue()));
    }

}
