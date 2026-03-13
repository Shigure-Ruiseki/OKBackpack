package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.helper.ItemStackHelpers;

public interface IBasicFilterable {

    String FILTER_ITEMS_TAG = "FilterItems";
    String FILTER_TYPE_TAG = "FilterType";

    UpgradeItemStackHandler getFilterItems();

    void setFilterItems(UpgradeItemStackHandler handler);

    FilterType getFilterType();

    void setFilterType(FilterType type);

    enum FilterType {
        WHITELIST,
        BLACKLIST;
    }

    default boolean checkFilter(ItemStack check) {
        switch (getFilterType()) {
            case WHITELIST:
                for (ItemStack s : getFilterItems().getStacks()) {
                    if (ItemStackHelpers.areItemsEqualIgnoreDurability(s, check)) {
                        return true;
                    }
                }
                return false;
            case BLACKLIST:
                for (ItemStack s : getFilterItems().getStacks()) {
                    if (ItemStackHelpers.areItemsEqualIgnoreDurability(s, check)) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }
}
