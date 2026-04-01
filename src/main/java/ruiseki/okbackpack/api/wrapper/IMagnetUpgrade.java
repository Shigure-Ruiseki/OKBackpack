package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface IMagnetUpgrade {

    String MAG_ITEM_TAG = "CollectItem";
    String MAG_EXP_TAG = "CollectExp";

    boolean isCollectItem();

    void setCollectItem(boolean enabled);

    boolean isCollectExp();

    void setCollectExp(boolean collectExp);

    boolean canCollectItem(ItemStack stack);

    default void toggleItem() {
        setCollectItem(!isCollectItem());
    }

    default void toggleExp() {
        setCollectExp(!isCollectExp());
    }
}
