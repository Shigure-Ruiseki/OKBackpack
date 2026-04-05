package ruiseki.okbackpack.client.gui.widget.updateGroup;

import ruiseki.okbackpack.api.widget.UpgradeSlotGroupRegistry;
import ruiseki.okcore.init.IInitListener;

public class UpgradeSlotGroupRegisters implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            UpgradeSlotGroupRegistry.register(new CommonFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new AdvancedCommonFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new FeedingFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new AdvancedFeedingFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new CraftingSlotGroup());
            UpgradeSlotGroupRegistry.register(new SmeltingSlotGroupFactory());
        }
    }
}
