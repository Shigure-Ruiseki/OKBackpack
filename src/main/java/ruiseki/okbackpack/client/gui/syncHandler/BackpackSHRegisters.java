package ruiseki.okbackpack.client.gui.syncHandler;

import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.api.SortBy;
import ruiseki.okbackpack.api.upgrade.BackpackSHRegistry;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.helpers.BackpackSettingsTemplate;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.bogosorter.BackpackBogoSorterServerCompat;
import ruiseki.okcore.init.IInitListener;

public class BackpackSHRegisters implements IInitListener {

    public static final String UPDATE_SET_SORT_TYPE = "update_set_sort_type";
    public static final String UPDATE_SORT_INV = "update_sort_inv";
    public static final String UPDATE_TRANSFER_TO_BACKPACK_INV = "update_transfer_to_backpack_inv";
    public static final String UPDATE_TRANSFER_TO_PLAYER_INV = "update_transfer_to_player_inv";
    public static final String UPDATE_SETTING = "update_setting";
    public static final String UPDATE_SEARCH_PHRASE = "update_search_phrase";
    public static final String UPDATE_SET_NO_SORT_COLOR = "update_set_no_sort_color";
    public static final String UPDATE_SAVE_SETTINGS_PRESET = "update_save_settings_preset";
    public static final String UPDATE_LOAD_SETTINGS_PRESET = "update_load_settings_preset";
    public static final String UPDATE_DELETE_SETTINGS_PRESET = "update_delete_settings_preset";
    public static final String UPDATE_IMPORT_SETTINGS_PRESET = "update_import_settings_preset";
    public static final String UPDATE_TAB_INDEX = "update_tab_index";
    public static final String UPDATE_BOGO_SORT_INV = "update_bogo_sort_inv";
    public static final String DEPLOY_SLEEPING_BAG = "update_deploy_sleeping_bag";

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {

            BackpackSHRegistry.registerServer(UPDATE_SET_SORT_TYPE, (handler, buf) -> {
                SortBy sortBy = NetworkUtils.readEnumValue(buf, SortBy.class);
                handler.wrapper.setSortType(sortBy);
            });

            BackpackSHRegistry.registerServer(UPDATE_SORT_INV, (handler, buf) -> {
                boolean reverse = buf.readBoolean();
                BackpackInventoryHelpers.sortInventory(handler.wrapper, reverse);
                handler.wrapper.markDirty();
            });

            if (Mods.InventoryBogoSorter.isModLoaded()) {
                BackpackBogoSorterServerCompat.register();
            }

            BackpackSHRegistry.registerServer(UPDATE_TRANSFER_TO_BACKPACK_INV, (handler, buf) -> {
                boolean transferMatched = buf.readBoolean();
                BackpackInventoryHelpers
                    .transferPlayerInventoryToBackpack(handler.wrapper, handler.playerInv, transferMatched);
            });

            BackpackSHRegistry.registerServer(UPDATE_TRANSFER_TO_PLAYER_INV, (handler, buf) -> {
                boolean transferMatched = buf.readBoolean();
                BackpackInventoryHelpers
                    .transferBackpackToPlayerInventory(handler.wrapper, handler.playerInv, transferMatched);
            });

            BackpackSHRegistry.registerServer(UPDATE_SETTING, (handler, buf) -> {
                boolean usePlayerSettings = buf.readBoolean();
                boolean lock = buf.readBoolean();
                String playerUuid = buf.readStringFromBuffer(36);
                boolean tab = buf.readBoolean();
                boolean shiftClick = buf.readBoolean();
                boolean keepSearch = buf.readBoolean();
                int mainColor = buf.readInt();
                int accentColor = buf.readInt();
                handler.wrapper.setUsePlayerSettings(usePlayerSettings);
                handler.wrapper.setLockStorage(lock);
                handler.wrapper.setPlayerUUID(playerUuid);
                handler.wrapper.setKeepTab(tab);
                handler.wrapper.setShiftClickIntoOpenTab(shiftClick);
                handler.wrapper.setKeepSearchPhrase(keepSearch);
                handler.wrapper.setColors(mainColor, accentColor);

                if (usePlayerSettings) {
                    BackpackProperty property = BackpackProperty.get(
                        handler.getSyncManager()
                            .getPlayer());
                    if (property != null) {
                        property.setLockBackpack(lock);
                        property.setKeepTab(tab);
                        property.setShiftClickIntoOpenTab(shiftClick);
                        property.setKeepSearchPhrase(keepSearch);
                        property.applySettingsToWrapper(handler.wrapper);
                        property.syncGlobalSettingsToOwnedBackpacks();
                    }
                }
            });

            BackpackSHRegistry.registerServer(UPDATE_SEARCH_PHRASE, (handler, buf) -> {
                handler.wrapper.setSearchPhrase(buf.readStringFromBuffer(256));
                handler.wrapper.markDirty();
            });

            BackpackSHRegistry.registerServer(UPDATE_SET_NO_SORT_COLOR, (handler, buf) -> {
                handler.wrapper.setNoSortColorIndex(buf.readInt());
                handler.wrapper.markDirty();
            });

            BackpackSHRegistry.registerServer(UPDATE_SAVE_SETTINGS_PRESET, (handler, buf) -> {
                int index = buf.readInt();
                String name = buf.readStringFromBuffer(128);
                handler.wrapper.saveSettingsPreset(index, name);
            });

            BackpackSHRegistry.registerServer(UPDATE_LOAD_SETTINGS_PRESET, (handler, buf) -> {
                int index = buf.readInt();
                if (handler.wrapper.loadSettingsPreset(index) && handler.wrapper.isUsePlayerSettings()) {
                    BackpackProperty property = BackpackProperty.get(
                        handler.getSyncManager()
                            .getPlayer());
                    if (property != null) {
                        property.setLockBackpack(handler.wrapper.isLockStorage());
                        property.setKeepTab(handler.wrapper.isKeepTab());
                        property.setShiftClickIntoOpenTab(handler.wrapper.isShiftClickIntoOpenTab());
                        property.setKeepSearchPhrase(handler.wrapper.isKeepSearchPhrase());
                        property.syncGlobalSettingsToOwnedBackpacks();
                    }
                }
            });

            BackpackSHRegistry.registerServer(
                UPDATE_DELETE_SETTINGS_PRESET,
                (handler, buf) -> { handler.wrapper.deleteSettingsPreset(buf.readInt()); });

            BackpackSHRegistry.registerServer(UPDATE_IMPORT_SETTINGS_PRESET, (handler, buf) -> {
                String name = buf.readStringFromBuffer(128);
                BackpackSettingsTemplate template = BackpackSettingsTemplate
                    .fromNBT(buf.readNBTTagCompoundFromBuffer(), handler.wrapper.getSlots());
                handler.wrapper.addSettingsPreset(name, template);
            });

            BackpackSHRegistry.registerServer(UPDATE_TAB_INDEX, (handler, buf) -> {
                int index = buf.readInt();
                handler.wrapper.setTabStartIndex(index);
            });

        }
    }

}
