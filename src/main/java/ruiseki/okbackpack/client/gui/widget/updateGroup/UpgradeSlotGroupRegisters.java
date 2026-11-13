package ruiseki.okbackpack.client.gui.widget.updateGroup;

import ruiseki.okbackpack.api.widget.UpgradeSlotGroupRegistry;
import ruiseki.okbackpack.compat.Mods;
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
            UpgradeSlotGroupRegistry.register(new JukeboxStorageSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new AdvancedJukeboxStorageSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new SmeltingSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new AnvilSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new BatterySlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new TankSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new RefillFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new AdvancedRefillFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new DepositFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new AdvancedDepositFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new RestockFilterSlotGroupFactory());
            UpgradeSlotGroupRegistry.register(new AdvancedRestockFilterSlotGroupFactory());
            if (Mods.Thaumcraft.isModLoaded()) {
                UpgradeSlotGroupRegistry.register(new ArcaneCraftingSlotGroupFactory());
                UpgradeSlotGroupRegistry.register(new EnergizedNodeSlotGroupFactory());
            }
        }
    }
}
