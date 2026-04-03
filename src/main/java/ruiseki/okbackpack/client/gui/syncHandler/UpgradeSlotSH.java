package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.api.wrapper.IAdvancedFilterable;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.IFeedingUpgrade;
import ruiseki.okbackpack.api.wrapper.IFilterUpgrade;
import ruiseki.okbackpack.api.wrapper.IMagnetUpgrade;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IVoidUpgrade;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.item.wrapper.AdvancedFeedingUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperFactory;

public class UpgradeSlotSH extends ItemSlotSH {

    public static final int UPDATE_UPGRADE_TAB_STATE = 6;
    public static final int UPDATE_UPGRADE_TOGGLE = 7;
    public static final int UPDATE_BASIC_FILTERABLE = 8;
    public static final int UPDATE_ADVANCED_FILTERABLE = 9;
    public static final int UPDATE_ADVANCED_FEEDING = 10;
    public static final int UPDATE_FILTER = 11;
    public static final int UPDATE_MAGNET = 12;
    public static final int UPDATE_CRAFTING = 13;
    public static final int UPDATE_VOID = 14;
    public static final int UPDATE_CRAFTING_R = 15;
    public static final int UPDATE_CRAFTING_G = 16;
    public static final int UPDATE_CRAFTING_C = 17;
    public static final int UPDATE_DIRTY = 18;

    public final BackpackWrapper wrapper;
    public final BackpackPanel panel;

    public UpgradeSlotSH(ModularSlot slot, BackpackWrapper wrapper, BackpackPanel panel) {
        super(slot);
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        switch (id) {
            case UPDATE_UPGRADE_TAB_STATE:
                updateTabState(buf);
                break;
            case UPDATE_UPGRADE_TOGGLE:
                updateToggleable();
                break;
            case UPDATE_BASIC_FILTERABLE:
                updateBasicFilterable(buf);
                break;
            case UPDATE_ADVANCED_FILTERABLE:
                updateAdvancedFilterable(buf);
                break;
            case UPDATE_ADVANCED_FEEDING:
                updateAdvanceFeedingUpgrade(buf);
                break;
            case UPDATE_FILTER:
                updateFilterUpgrade(buf);
                break;
            case UPDATE_MAGNET:
                updateMagnetUpgrade(buf);
                break;
            case UPDATE_CRAFTING:
                updateCraftingUpgrade(buf);
                break;
            case UPDATE_VOID:
                updateVoidUpgrade(buf);
                break;
            case UPDATE_CRAFTING_R:
                updateRotated(buf);
                break;
            case UPDATE_CRAFTING_G:
                updateGrid(buf);
                break;
            case UPDATE_CRAFTING_C:
                updateClear(buf);
                break;
            case UPDATE_DIRTY:
                updateDirty(buf);
                break;
            default:
                super.readOnServer(id, buf);
                return;
        }
        wrapper.markDirty();
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        super.readOnClient(id, buf);
        if (id == UPDATE_UPGRADE_TAB_STATE || id == UPDATE_UPGRADE_TOGGLE
            || id == UPDATE_BASIC_FILTERABLE
            || id == UPDATE_ADVANCED_FILTERABLE
            || id == UPDATE_ADVANCED_FEEDING
            || id == UPDATE_FILTER
            || id == UPDATE_MAGNET
            || id == UPDATE_CRAFTING
            || id == UPDATE_VOID
            || id == UPDATE_CRAFTING_R
            || id == UPDATE_CRAFTING_G
            || id == UPDATE_CRAFTING_C

        ) {
            wrapper.syncToServer();
        }
    }

    private UpgradeWrapperBase getWrapper() {
        ItemStack stack = getSlot().getStack();
        if (stack == null) return null;
        return UpgradeWrapperFactory.createWrapper(stack, this.wrapper);
    }

    private void updateTabState(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (wrapper == null) return;
        wrapper.setTabOpened(buf.readBoolean());
    }

    private void updateToggleable() {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof IToggleable toggleWrapper)) return;
        toggleWrapper.toggle();
    }

    private void updateBasicFilterable(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof IBasicFilterable upgradeWrapper)) return;
        IBasicFilterable.FilterType type = NetworkUtils.readEnumValue(buf, IBasicFilterable.FilterType.class);
        upgradeWrapper.setFilterType(type);
    }

    private void updateAdvancedFilterable(PacketBuffer buf) throws IOException {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof IAdvancedFilterable upgradeWrapper)) return;

        // APPLY BASIC SETTINGS
        upgradeWrapper.setFilterType(NetworkUtils.readEnumValue(buf, IAdvancedFilterable.FilterType.class));
        upgradeWrapper.setMatchType(NetworkUtils.readEnumValue(buf, IAdvancedFilterable.MatchType.class));
        upgradeWrapper.setIgnoreDurability(buf.readBoolean());
        upgradeWrapper.setIgnoreNBT(buf.readBoolean());

        int size = buf.readInt();
        List<String> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(buf.readStringFromBuffer(100));
        }

        upgradeWrapper.setOreDictEntries(list);
    }

    private void updateAdvanceFeedingUpgrade(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof AdvancedFeedingUpgradeWrapper upgradeWrapper)) return;
        upgradeWrapper
            .setHungerFeedingStrategy(NetworkUtils.readEnumValue(buf, IFeedingUpgrade.FeedingStrategy.Hunger.class));
        upgradeWrapper
            .setHealthFeedingStrategy(NetworkUtils.readEnumValue(buf, IFeedingUpgrade.FeedingStrategy.HEALTH.class));
    }

    private void updateFilterUpgrade(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof IFilterUpgrade upgradeWrapper)) return;
        upgradeWrapper.setFilterWay(NetworkUtils.readEnumValue(buf, IFilterUpgrade.FilterWayType.class));
    }

    private void updateMagnetUpgrade(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof IMagnetUpgrade upgrade)) return;
        upgrade.setCollectItem(buf.readBoolean());
        upgrade.setCollectExp(buf.readBoolean());
    }

    private void updateCraftingUpgrade(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof ICraftingUpgrade upgradeWrapper)) return;
        upgradeWrapper.setCraftingDes(NetworkUtils.readEnumValue(buf, ICraftingUpgrade.CraftingDestination.class));
        upgradeWrapper.setUseBackpack(buf.readBoolean());
    }

    private void updateVoidUpgrade(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof IVoidUpgrade upgradeWrapper)) return;
        upgradeWrapper.setVoidType(NetworkUtils.readEnumValue(buf, IVoidUpgrade.VoidType.class));
        upgradeWrapper.setVoidInput(NetworkUtils.readEnumValue(buf, IVoidUpgrade.VoidInput.class));
    }

    public void updateRotated(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof ICraftingUpgrade upgradeWrapper)) return;
        boolean clockwise = buf.readBoolean();
        ItemStackHandler stackHandler = upgradeWrapper.getStorage();
        BackpackInventoryHelpers.rotated(stackHandler, clockwise);
        wrapper.markDirty();
    }

    public void updateGrid(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof ICraftingUpgrade upgradeWrapper)) return;
        boolean balance = buf.readBoolean();
        ItemStackHandler stackHandler = upgradeWrapper.getStorage();
        if (balance) {
            BackpackInventoryHelpers.balance(stackHandler);
        } else {
            BackpackInventoryHelpers.spread(stackHandler);
        }
        wrapper.markDirty();
    }

    public void updateClear(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (!(wrapper instanceof ICraftingUpgrade upgradeWrapper)) return;
        int ordinal = buf.readInt();
        BackpackInventoryHelpers.clear(panel, upgradeWrapper.getStorage(), ordinal);
        panel.player.inventory.markDirty();
        wrapper.markDirty();
    }

    private void updateDirty(PacketBuffer buf) {
        UpgradeWrapperBase wrapper = getWrapper();
        if (wrapper == null) return;
        boolean isDirty = buf.readBoolean();
        wrapper.setDirty(isDirty);
    }

}
