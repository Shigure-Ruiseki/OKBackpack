package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapperFactory;

public class UpgradeWrapperFactory {

    @SuppressWarnings("unchecked")
    public static <W extends UpgradeWrapperBase> W createWrapper(ItemStack stack, IStorageWrapper storage) {
        if (stack == null) {
            return null;
        }
        Item item = stack.getItem();
        if (!(item instanceof IUpgradeWrapperFactory<?>factory)) {
            return null;
        }
        return (W) factory.createWrapper(stack, storage);
    }
}
