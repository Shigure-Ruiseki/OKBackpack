package ruiseki.okbackpack.client.gui.syncHandler;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.api.SortType;
import ruiseki.okbackpack.api.upgrade.BackpackSHRegistry;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okbackpack.common.block.TEBackpack;
import ruiseki.okbackpack.common.entity.properties.BackpackProperty;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.helpers.BackpackSettingsTemplate;
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
    public static final String DEPLOY_SLEEPING_BAG = "update_deploy_sleeping_bag";

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {

            BackpackSHRegistry.registerServer(UPDATE_SET_SORT_TYPE, (handler, buf) -> {
                SortType sortType = NetworkUtils.readEnumValue(buf, SortType.class);
                handler.wrapper.setSortType(sortType);
            });

            BackpackSHRegistry.registerServer(UPDATE_SORT_INV, (handler, buf) -> {
                for (int i = 0; i < handler.wrapper.getSlots(); i++) {
                    handler.wrapper.setStackInSlot(i, buf.readItemStackFromBuffer());
                }
            });

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
                handler.wrapper.setUsePlayerSettings(usePlayerSettings);
                handler.wrapper.setLockStorage(lock);
                handler.wrapper.setPlayerUUID(playerUuid);
                handler.wrapper.setKeepTab(tab);
                handler.wrapper.setShiftClickIntoOpenTab(shiftClick);
                handler.wrapper.setKeepSearchPhrase(keepSearch);

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

            BackpackSHRegistry.registerServer(DEPLOY_SLEEPING_BAG, (handler, buf) -> { deploySleepingBag(handler); });
        }
    }

    public void deploySleepingBag(BackpackSH backpackSH) {
        EntityPlayer player = backpackSH.getSyncManager()
            .getPlayer();

        World world = player.worldObj;
        TileEntity tile = backpackSH.panel.getTile();
        if (tile != null && world.getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) instanceof TEBackpack te) {
            if (!te.isSleepingBagDeployed()) {
                int[] can = BlockSleepingBag
                    .canDeploySleepingBag(world, player, tile.xCoord, tile.yCoord, tile.zCoord, true);
                if (can[0] > -1) {
                    if (te.deploySleepingBag(player, world, can[0], can[1], can[2], can[3])) {
                        player.closeScreen();
                    }
                } else if (!world.isRemote) {
                    player.addChatComponentMessage(new ChatComponentTranslation("messages.backpack.cant.bag"));
                }
            } else {
                te.removeSleepingBag(world);
            }
            player.closeScreen();
        } else if (tile == null) {
            int[] can = BlockSleepingBag.canDeploySleepingBag(
                world,
                player,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ - 1,
                false);
            if (can[0] > -1) {
                if (backpackSH.wrapper.deploySleepingBag(player, world, can[0], can[1], can[2], can[3])) {
                    Block portableBag = world.getBlock(can[1], can[2], can[3]);
                    if (portableBag instanceof BlockSleepingBag) {
                        BackpackProperty.get(player)
                            .setSleepingInPortableBag(true);
                        ((BlockSleepingBag) portableBag)
                            .onPortableBlockActivated(world, player, can[1], can[2], can[3]);
                    }
                }
            } else if (!world.isRemote) {
                player.addChatComponentMessage(new ChatComponentTranslation("messages.backpack.cant.bag"));
            }
            player.closeScreen();
        }
    }
}
