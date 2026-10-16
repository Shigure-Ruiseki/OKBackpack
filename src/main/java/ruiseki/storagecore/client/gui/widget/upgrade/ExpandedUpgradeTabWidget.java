package ruiseki.storagecore.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.drawable.ItemDrawable;

import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.sync.StorageSHRegisters;
import ruiseki.storagecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.storagecore.client.gui.widget.ExpandedTabWidget;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public abstract class ExpandedUpgradeTabWidget<U extends UpgradeWrapperBase> extends ExpandedTabWidget {

    protected abstract U getWrapper();

    protected UpgradeSlotSH slotSyncHandler = null;

    protected IStorageWrapper storageWrapper;
    protected StoragePanel storagePanel;
    protected int slotIndex;

    public ExpandedUpgradeTabWidget(int slotIndex, int coveredTabSize, ItemStack delegatedIconStack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey, int width) {
        super(coveredTabSize, new ItemDrawable(delegatedIconStack), titleKey, width);
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;
        this.syncHandler("upgrades", this.slotIndex);
    }

    public ExpandedUpgradeTabWidget(int slotIndex, int coveredTabSize, ItemStack delegatedIconStack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
        this(slotIndex, coveredTabSize, delegatedIconStack, storageWrapper, storagePanel, titleKey, 80);
    }

    @Override
    public void updateTabState() {
        this.getStorageWrapper()
            .setOpenTabId(this.slotIndex);
        this.getStoragePanel()
            .getStorageSH()
            .syncToServer(StorageSHRegisters.OPENED_ID, buffer -> buffer.writeInt(this.slotIndex));
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
}
