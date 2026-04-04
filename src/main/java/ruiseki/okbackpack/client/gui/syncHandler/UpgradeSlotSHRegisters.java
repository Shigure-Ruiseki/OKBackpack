package ruiseki.okbackpack.client.gui.syncHandler;

import java.util.ArrayList;
import java.util.List;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;

import ruiseki.okbackpack.api.upgrade.UpgradeSlotSHRegistry;
import ruiseki.okbackpack.api.wrapper.IAdvancedFilterable;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.ICompactingUpgrade;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.IFeedingUpgrade;
import ruiseki.okbackpack.api.wrapper.IFilterUpgrade;
import ruiseki.okbackpack.api.wrapper.IMagnetUpgrade;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IVoidUpgrade;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.item.wrapper.AdvancedFeedingUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;
import ruiseki.okcore.init.IInitListener;

public class UpgradeSlotSHRegisters implements IInitListener {

    public static final String UPDATE_UPGRADE_TAB_STATE = "update_upgrade_tab_state";
    public static final String UPDATE_UPGRADE_TOGGLE = "update_upgrade_toggle";
    public static final String UPDATE_BASIC_FILTERABLE = "update_basic_filterable";
    public static final String UPDATE_ADVANCED_FILTERABLE = "update_advanced_filterable";
    public static final String UPDATE_ADVANCED_FEEDING = "update_advanced_feeding";
    public static final String UPDATE_FILTER = "update_filter";
    public static final String UPDATE_MAGNET = "update_magnet";
    public static final String UPDATE_CRAFTING = "update_crafting";
    public static final String UPDATE_VOID = "update_void";
    public static final String UPDATE_CRAFTING_R = "update_crafting_r";
    public static final String UPDATE_CRAFTING_G = "update_crafting_g";
    public static final String UPDATE_CRAFTING_C = "update_crafting_c";
    public static final String UPDATE_DIRTY = "update_dirty";
    public static final String UPDATE_COMPACTING = "update_compacting";

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {

            UpgradeSlotSHRegistry.registerServer(UPDATE_UPGRADE_TAB_STATE, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (wrapper == null) return;
                wrapper.setTabOpened(buf.readBoolean());
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_UPGRADE_TOGGLE, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof IToggleable toggle)) return;
                toggle.toggle();
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_BASIC_FILTERABLE, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof IBasicFilterable upgrade)) return;
                upgrade.setFilterType(NetworkUtils.readEnumValue(buf, IBasicFilterable.FilterType.class));
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_ADVANCED_FILTERABLE, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof IAdvancedFilterable upgrade)) return;

                upgrade.setFilterType(NetworkUtils.readEnumValue(buf, IAdvancedFilterable.FilterType.class));
                upgrade.setMatchType(NetworkUtils.readEnumValue(buf, IAdvancedFilterable.MatchType.class));
                upgrade.setIgnoreDurability(buf.readBoolean());
                upgrade.setIgnoreNBT(buf.readBoolean());

                int size = buf.readInt();
                List<String> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) list.add(buf.readStringFromBuffer(100));
                upgrade.setOreDictEntries(list);
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_ADVANCED_FEEDING, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof AdvancedFeedingUpgradeWrapper upgrade)) return;
                upgrade.setHungerFeedingStrategy(
                    NetworkUtils.readEnumValue(buf, IFeedingUpgrade.FeedingStrategy.Hunger.class));
                upgrade.setHealthFeedingStrategy(
                    NetworkUtils.readEnumValue(buf, IFeedingUpgrade.FeedingStrategy.HEALTH.class));
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_FILTER, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof IFilterUpgrade upgrade)) return;
                upgrade.setFilterWay(NetworkUtils.readEnumValue(buf, IFilterUpgrade.FilterWayType.class));
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_MAGNET, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof IMagnetUpgrade upgrade)) return;
                upgrade.setCollectItem(buf.readBoolean());
                upgrade.setCollectExp(buf.readBoolean());
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_CRAFTING, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof ICraftingUpgrade upgrade)) return;
                upgrade.setCraftingDes(NetworkUtils.readEnumValue(buf, ICraftingUpgrade.CraftingDestination.class));
                upgrade.setUseBackpack(buf.readBoolean());
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_VOID, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof IVoidUpgrade upgrade)) return;
                upgrade.setVoidType(NetworkUtils.readEnumValue(buf, IVoidUpgrade.VoidType.class));
                upgrade.setVoidInput(NetworkUtils.readEnumValue(buf, IVoidUpgrade.VoidInput.class));
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_CRAFTING_R, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof ICraftingUpgrade upgrade)) return;
                boolean clockwise = buf.readBoolean();
                ItemStackHandler storage = upgrade.getStorage();
                BackpackInventoryHelpers.rotated(storage, clockwise);
                wrapper.markDirty();
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_CRAFTING_G, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof ICraftingUpgrade upgrade)) return;
                boolean balance = buf.readBoolean();
                ItemStackHandler storage = upgrade.getStorage();
                if (balance) BackpackInventoryHelpers.balance(storage);
                else BackpackInventoryHelpers.spread(storage);
                wrapper.markDirty();
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_CRAFTING_C, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof ICraftingUpgrade upgrade)) return;
                int ordinal = buf.readInt();
                BackpackInventoryHelpers.clear(slot.panel, upgrade.getStorage(), ordinal);
                slot.panel.getPlayer().inventory.markDirty();
                wrapper.markDirty();
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_DIRTY, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (wrapper == null) return;
                wrapper.setDirty(buf.readBoolean());
            });

            UpgradeSlotSHRegistry.registerServer(UPDATE_COMPACTING, (slot, buf) -> {
                UpgradeWrapperBase wrapper = slot.getWrapper();
                if (!(wrapper instanceof ICompactingUpgrade upgradeWrapper)) return;
                upgradeWrapper.setOnlyReversible(buf.readBoolean());
            });

        }
    }

}
