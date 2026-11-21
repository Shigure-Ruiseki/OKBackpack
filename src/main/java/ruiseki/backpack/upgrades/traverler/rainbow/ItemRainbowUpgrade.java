package ruiseki.backpack.upgrades.traverler.rainbow;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemRainbowUpgrade extends ItemTravelerUpgrade<RainbowUpgradeWrapper> {

    public static final UpgradeType<RainbowUpgradeWrapper> TYPE = new UpgradeType<>(RainbowUpgradeWrapper::new);

    public ItemRainbowUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<RainbowUpgradeWrapper> getType() {
        return TYPE;
    }
}
