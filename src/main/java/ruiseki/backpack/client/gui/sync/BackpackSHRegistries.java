package ruiseki.backpack.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.backpack.settings.BackpackMainSettingsCategory;
import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegistry;
import ruiseki.okstoragecore.settings.SettingsManager;
import ruiseki.okstoragecore.settings.main.MainSettingsCategory;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.settings.nosort.NoSortSettingsCategory;

public class BackpackSHRegistries {

    public static final StorageSHRegistry.Entry<BackpackSH> REGISTRY = StorageSHRegistry.get(BackpackSH.class);

    public static final ResourceLocation SET_ANOTHER_PLAYER_CAN_OPEN_SETTING = new ResourceLocation(
        Reference.MOD_ID,
        "storage_set_another_player_can_open_setting");

    public static void load() {
        REGISTRY.registerServer(SET_ANOTHER_PLAYER_CAN_OPEN_SETTING, (handler, buf) -> {
            MainSettingsCategory<?> settings = getGlobalSettings(handler.getWrapper());
            SettingsManager.setSettingByMode(
                handler.getPlayer(),
                settings.getPlayerSettingsTagName(),
                settings,
                BackpackMainSettingsCategory.ANOTHER_PLAYER_CAN_OPEN,
                buf.readBoolean(),
                buf.readBoolean());
        });

    }

    public static MemorySettingsCategory getMemorySettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }

    public static NoSortSettingsCategory getNoSortingSettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getTypeCategory(NoSortSettingsCategory.class);
    }

    public static MainSettingsCategory<?> getGlobalSettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getGlobalSettingsCategory();
    }
}
