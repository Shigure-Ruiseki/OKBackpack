package ruiseki.okbackpack.common.item.travelers.magma;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemMagmaCubeUpgrade extends ItemTravelersUpgradeBase<MagmaCubeUpgradeWrapper> {

    public ItemMagmaCubeUpgrade() {
        super("magma_cube_upgrade", "tooltip.backpack.magma_cube_upgrade");
    }

    @Override
    public MagmaCubeUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> consumer) {
        return new MagmaCubeUpgradeWrapper(stack, storage, consumer);
    }
}
