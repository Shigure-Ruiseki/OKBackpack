package ruiseki.backpack.upgrades.traverler.bookshelf;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IBookshelfUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class BookShelfUpgradeWrapper extends UpgradeWrapperBase<BookShelfUpgradeWrapper, ItemBookShelfUpgrade>
    implements IBookshelfUpgrade {

    public BookShelfUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public float getEnchantPowerBonus() {
        return 10.0f;
    }
}
