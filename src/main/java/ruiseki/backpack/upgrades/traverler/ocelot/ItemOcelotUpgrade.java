package ruiseki.backpack.upgrades.traverler.ocelot;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemOcelotUpgrade extends ItemTravelerUpgrade<OcelotUpgradeWrapper> {

    public static final UpgradeType<OcelotUpgradeWrapper> TYPE = new UpgradeType<>(OcelotUpgradeWrapper::new);

    public ItemOcelotUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<OcelotUpgradeWrapper> getType() {
        return TYPE;
    }
}
