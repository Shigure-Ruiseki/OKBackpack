package ruiseki.okstoragecore.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegisters;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public abstract class ExpandedUpgradeTab<U extends UpgradeWrapperBase<U, ?>> extends ExpandedTab {

    protected UpgradeSlotSH slotSyncHandler = null;

    protected IStorageWrapper storageWrapper;
    protected StoragePanel storagePanel;
    protected int slotIndex;
    protected U wrapper;

    public ExpandedUpgradeTab(int slotIndex, U wrapper, ItemStack stack, IStorageWrapper storageWrapper,
        StoragePanel storagePanel, String titleKey) {
        super(new ItemDrawable(stack), titleKey);
        this.wrapper = wrapper;
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;
        this.syncHandler("upgrades", this.getSlotIndex());

        this.getContentsColumn()
            .addChild(this.addUpgradeColumn(), 1);
    }

    protected U getWrapper() {
        return this.wrapper;
    }

    @Override
    public void updateTabState() {
        int currentTabId = this.storageWrapper.getOpenTabId()
            .orElse(-1);
        int newTabId = currentTabId == this.slotIndex ? -1 : this.slotIndex;
        this.storageWrapper.setOpenTabId(newTabId);
        this.storagePanel.getStorageSH()
            .syncToServer(StorageSHRegisters.OPENED_ID, buffer -> buffer.writeInt(newTabId));
        this.storagePanel.updateUpgradeTabs();
    }

    protected Flow addUpgradeColumn() {
        return Flow.column()
            .coverChildren()
            .childPadding(2);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        if (syncOrValue instanceof UpgradeSlotSH) {
            this.slotSyncHandler = (UpgradeSlotSH) syncOrValue;
        }
        return this.slotSyncHandler != null;
    }

    public UpgradeSlotSH getSlotSyncHandler() {
        return this.slotSyncHandler;
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public @NotNull UpgradeSlotSH getSyncHandler() {
        return (UpgradeSlotSH) super.getSyncHandler();
    }
}
