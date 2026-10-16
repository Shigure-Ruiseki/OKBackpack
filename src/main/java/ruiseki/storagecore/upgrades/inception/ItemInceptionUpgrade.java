package ruiseki.storagecore.upgrades.inception;

import ruiseki.storagecore.upgrades.ItemUpgradeBase;
import ruiseki.storagecore.upgrades.UpgradeType;

public class ItemInceptionUpgrade extends ItemUpgradeBase<InceptionUpgradeWrapper> {

    public static final UpgradeType<InceptionUpgradeWrapper> TYPE = new UpgradeType<>(InceptionUpgradeWrapper::new);

    public ItemInceptionUpgrade() {
        super(null);
    }

    @Override
    public UpgradeType<InceptionUpgradeWrapper> getType() {
        return TYPE;
    }
}
