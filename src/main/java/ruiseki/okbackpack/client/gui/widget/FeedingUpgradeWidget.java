package ruiseki.okbackpack.client.gui.widget;

import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.common.item.wrapper.FeedingUpgradeWrapper;

public class FeedingUpgradeWidget extends BasicExpandedTabWidget<FeedingUpgradeWrapper> {

    public FeedingUpgradeWidget(int slotIndex, FeedingUpgradeWrapper wrapper, String titleKey) {
        super(slotIndex, wrapper, ModItems.FEEDING_UPGRADE.newItemStack(), titleKey, "feeding_filter");
    }
}
