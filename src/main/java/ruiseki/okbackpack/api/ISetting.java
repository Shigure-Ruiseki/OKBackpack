package ruiseki.okbackpack.api;

import ruiseki.okbackpack.common.helpers.BackpackSettingsTemplate;

public interface ISetting {

    String USE_PLAYER_SETTINGS_TAG = "UsePlayerSettings";
    String NO_SORT_COLOR_INDEX_TAG = "NoSortColorIndex";
    String SETTINGS_PRESETS_TAG = "SettingsPresets";

    int getSettingsPresetCount();

    String getSettingsPresetName(int index);

    void saveSettingsPreset(int index, String name);

    int addSettingsPreset(String name, BackpackSettingsTemplate template);

    boolean loadSettingsPreset(int index);

    int deleteSettingsPreset(int index);

}
