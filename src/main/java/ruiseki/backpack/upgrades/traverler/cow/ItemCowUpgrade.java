package ruiseki.backpack.upgrades.traverler.cow;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemCowUpgrade extends ItemTravelerUpgrade<CowUpgradeWrapper> {

    public static final UpgradeType<CowUpgradeWrapper> TYPE = new UpgradeType<>(CowUpgradeWrapper::new);

    public ItemCowUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<CowUpgradeWrapper> getType() {
        return TYPE;
    }
}
