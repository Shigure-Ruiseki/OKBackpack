package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface ICompactingUpgrade extends ITickable, IToggleable {

    String ONLY_REVERSIBLE_TAG = "OnlyReversible";

    boolean allowsGrid3x3();

    boolean isOnlyReversible();

    void setOnlyReversible(boolean onlyReversible);

    void compactInventory();

    boolean checkFilter(ItemStack stack);
}
