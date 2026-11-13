package ruiseki.backpack.upgrades.traverler.bookshelf;

import ruiseki.backpack.upgrades.traverler.ItemTravelerUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeCountLimitConfig;
import ruiseki.okstoragecore.upgrades.UpgradeType;

public class ItemBookShelfUpgrade extends ItemTravelerUpgrade<BookShelfUpgradeWrapper> {

    public static final UpgradeType<BookShelfUpgradeWrapper> TYPE = new UpgradeType<>(BookShelfUpgradeWrapper::new);

    public ItemBookShelfUpgrade(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        super(upgradeTypeLimitConfig);
    }

    @Override
    public UpgradeType<BookShelfUpgradeWrapper> getType() {
        return TYPE;
    }
}
