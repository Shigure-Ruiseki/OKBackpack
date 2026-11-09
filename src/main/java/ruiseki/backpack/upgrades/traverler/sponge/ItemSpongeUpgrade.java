package ruiseki.backpack.upgrades.traverler.sponge;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemSpongeUpgrade extends ItemTravelerUpgrade<SpongeUpgradeWrapper> {

    public static final UpgradeType<SpongeUpgradeWrapper> TYPE = new UpgradeType<>(SpongeUpgradeWrapper::new);

    public ItemSpongeUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<SpongeUpgradeWrapper> getType() {
        return TYPE;
    }
}
