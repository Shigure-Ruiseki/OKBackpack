package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class UpgradeWrapperFactory {

    @SuppressWarnings("unchecked")
    public static <W extends UpgradeWrapper> W createWrapper(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        Item item = stack.getItem();
        if (!(item instanceof IUpgradeWrapperFactory<?>factory)) {
            return null;
        }
        return (W) factory.createWrapper(stack);
    }
}
