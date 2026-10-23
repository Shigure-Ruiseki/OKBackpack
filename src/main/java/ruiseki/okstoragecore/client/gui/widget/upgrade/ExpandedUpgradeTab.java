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

    public ExpandedUpgradeTab(int slotIndex, int coveredTabSize, U wrapper, ItemStack delegatedIconStack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey, int width) {
        super(coveredTabSize, new ItemDrawable(delegatedIconStack), titleKey, width);
        this.wrapper = wrapper;
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;
        this.syncHandler("upgrades", this.getSlotIndex());

        this.child(this.addUpgradeColumn());
    }

    public ExpandedUpgradeTab(int slotIndex, int coveredTabSize, U wrapper, ItemStack delegatedIconStack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
        this(slotIndex, coveredTabSize, wrapper, delegatedIconStack, storageWrapper, storagePanel, titleKey, 80);
    }

    protected U getWrapper() {
        return this.wrapper;
    }

    @Override
    public void updateTabState() {
        int currentTabId = this.getStorageWrapper()
            .getOpenTabId()
            .orElse(-1);
        int targetTabId = this.getSlotIndex();
        int newTabId = currentTabId == targetTabId ? -1 : targetTabId;
        this.getStorageWrapper()
            .setOpenTabId(newTabId);
        this.getStoragePanel()
            .getStorageSH()
            .syncToServer(StorageSHRegisters.OPENED_ID, buffer -> buffer.writeInt(newTabId));
        this.getStoragePanel()
            .updateUpgradeTabs();
    }

    protected Flow addUpgradeColumn() {
        return Flow.column()
            .pos(8, 28)
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
