package ruiseki.storagecore.upgrades;

import net.minecraft.item.ItemStack;

public interface IUpgradeItem<T extends IUpgradeWrapper> {

    UpgradeType<T> getType();

    default int getInventoryColumnsTaken() {
        return 0;
    }

    default ItemStack getCleanedUpgradeStack(ItemStack upgradeStack) {
        return upgradeStack;
    }

    int getUpgradesPerStorage(String storageType);

    int getUpgradesInGroupPerStorage(String storageType);

    default UpgradeGroup getUpgradeGroup() {
        return UpgradeGroup.NONE;
    }

    String getName();
}
