package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.common.item.energizednode.EnergizedNodeUpgradeWrapper;

public class EnergizedNodeUpgradeWidget extends ExpandedUpgradeTabWidget<EnergizedNodeUpgradeWrapper> {

    private final EnergizedNodeUpgradeWrapper wrapper;

    public EnergizedNodeUpgradeWidget(int slotIndex, EnergizedNodeUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, 2, stack, panel, titleKey, 75);
        this.wrapper = wrapper;
    }

    @Override
    protected EnergizedNodeUpgradeWrapper getWrapper() {
        return wrapper;
    }
}
