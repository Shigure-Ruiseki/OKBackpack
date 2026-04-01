package ruiseki.okbackpack.client.gui.widget;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.drawable.ItemDrawable;

import lombok.Getter;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;

public abstract class ExpandedUpgradeTabWidget<U extends UpgradeWrapperBase> extends ExpandedTabWidget {

    protected abstract U getWrapper();

    @Getter
    protected UpgradeSlotSH slotSyncHandler = null;

    public ExpandedUpgradeTabWidget(int slotIndex, int coveredTabSize, ItemStack delegatedIconStack, String titleKey,
        int width) {
        super(coveredTabSize, new ItemDrawable(delegatedIconStack), titleKey, width);
        this.syncHandler("upgrades", slotIndex);
    }

    public ExpandedUpgradeTabWidget(int slotIndex, int coveredTabSize, ItemStack delegatedIconStack, String titleKey) {
        this(slotIndex, coveredTabSize, delegatedIconStack, titleKey, 80);
    }

    public void updateTabState() {
        U wrapper = getWrapper();
        if (wrapper != null) {
            boolean newState = !wrapper.isTabOpened();
            wrapper.setTabOpened(newState);

            if (slotSyncHandler != null) {
                slotSyncHandler
                    .syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE, buf -> { buf.writeBoolean(newState); });
            }
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
