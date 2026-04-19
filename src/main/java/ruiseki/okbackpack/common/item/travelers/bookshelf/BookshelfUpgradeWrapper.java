package ruiseki.okbackpack.common.item.travelers.bookshelf;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IBookshelfUpgrade;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class BookshelfUpgradeWrapper extends UpgradeWrapperBase implements ITravelersUpgrade, IBookshelfUpgrade {

    public BookshelfUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public float getEnchantPowerBonus() {
        return 10.0f;
    }
}
