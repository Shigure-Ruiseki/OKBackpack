package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.drawable.ItemDrawable;

import lombok.Getter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public abstract class ExpandedUpgradeTabWidget<U extends UpgradeWrapperBase> extends ExpandedTabWidget {

    protected abstract U getWrapper();

    @Getter
    protected UpgradeSlotSH slotSyncHandler = null;

    @Getter
    protected IStoragePanel<?> storagePanel;

    public ExpandedUpgradeTabWidget(int slotIndex, int coveredTabSize, ItemStack delegatedIconStack,
        IStoragePanel<?> storagePanel, String titleKey, int width) {
        super(coveredTabSize, new ItemDrawable(delegatedIconStack), titleKey, width);
        this.syncHandler("upgrades", slotIndex);
        this.storagePanel = storagePanel;
    }

    public ExpandedUpgradeTabWidget(int slotIndex, int coveredTabSize, ItemStack delegatedIconStack,
        IStoragePanel<?> storagePanel, String titleKey) {
        this(slotIndex, coveredTabSize, delegatedIconStack, storagePanel, titleKey, 80);
    }

    @Override
    public void updateTabState() {
        U wrapper = getWrapper();
        if (wrapper == null) return;

        boolean isCurrentlyOpened = wrapper.isTabOpened();

        if (isCurrentlyOpened) {
            wrapper.setTabOpened(false);

            if (slotSyncHandler != null) {
                slotSyncHandler.syncToServer(
                    UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TAB_STATE),
                    buf -> buf.writeBoolean(false));
            }
            return;
        }

        for (int i = 0; i < storagePanel.getWrapper()
            .getUpgradeHandler()
            .getSlots(); i++) {

            IUpgradeWrapper w = storagePanel.getWrapper()
                .getUpgradeHandler()
                .getWrapperInSlot(i);

            if (w != null && w.isTabOpened()) {
                w.setTabOpened(false);

                storagePanel.getUpgradedSlotSH()[i].syncToServer(
                    UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TAB_STATE),
                    buf -> buf.writeBoolean(false));
            }
        }

        wrapper.setTabOpened(true);

        if (slotSyncHandler != null) {
            slotSyncHandler.syncToServer(
                UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_UPGRADE_TAB_STATE),
                buf -> buf.writeBoolean(true));
        }
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        if (syncOrValue instanceof UpgradeSlotSH) {
            slotSyncHandler = (UpgradeSlotSH) syncOrValue;
        }
        return slotSyncHandler != null;
    }
}
