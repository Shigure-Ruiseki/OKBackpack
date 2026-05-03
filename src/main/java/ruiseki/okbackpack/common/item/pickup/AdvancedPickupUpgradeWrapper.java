package ruiseki.okbackpack.common.item.pickup;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.helpers.InventoryInteractionHelpers.StackKey;
import ruiseki.okbackpack.common.item.AdvancedUpgradeWrapper;
import ruiseki.okbackpack.common.item.restock.RestockFilterType;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedPickupUpgradeWrapper extends AdvancedUpgradeWrapper implements IPickupUpgrade {

    public AdvancedPickupUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_pickup_settings";
    }

    public RestockFilterType getPickupFilterType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, PICKUP_FILTER_TYPE_TAG, RestockFilterType.ALLOW.ordinal());
        RestockFilterType[] types = RestockFilterType.values();
        if (ordinal < 0 || ordinal >= types.length) return RestockFilterType.ALLOW;
        return types[ordinal];
    }

    public void setPickupFilterType(RestockFilterType type) {
        if (type == null) type = RestockFilterType.ALLOW;
        ItemNBTHelpers.setInt(upgrade, PICKUP_FILTER_TYPE_TAG, type.ordinal());
        save();
    }

    @Override
    public boolean canPickup(ItemStack stack) {
        if (!isEnabled()) return false;

        RestockFilterType filterType = getPickupFilterType();

        if (filterType == RestockFilterType.STORAGE) {
            StackKey searchKey = new StackKey(stack);
            boolean inBackpack = false;
            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack stored = storage.getStackInSlot(i);
                if (searchKey.matches(stored)) {
                    inBackpack = true;
                    break;
                }
            }
            if (!inBackpack) return false;
            if (!hasAnyFilterItem()) return true;
            return super.checkFilter(stack);
        }

        if (!hasAnyFilterItem()) return true;
        boolean matchesFilter = super.checkFilter(stack);
        return (filterType == RestockFilterType.ALLOW) == matchesFilter;
    }

    private boolean hasAnyFilterItem() {
        BaseItemStackHandler filterItems = getFilterItems();
        for (int i = 0; i < filterItems.getSlots(); i++) {
            if (filterItems.getStackInSlot(i) != null) return true;
        }
        return false;
    }
}
