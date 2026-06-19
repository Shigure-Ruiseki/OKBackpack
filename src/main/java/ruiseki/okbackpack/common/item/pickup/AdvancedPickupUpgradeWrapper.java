package ruiseki.okbackpack.common.item.pickup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.helpers.InventoryInteractionHelpers.StackKey;
import ruiseki.okbackpack.common.item.AdvancedUpgradeWrapper;
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

    @Override
    public PickupFilterType getPickupFilterType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, PICKUP_FILTER_TYPE_TAG, PickupFilterType.ALLOW.ordinal());
        PickupFilterType[] types = PickupFilterType.values();
        if (ordinal < 0 || ordinal >= types.length) return PickupFilterType.ALLOW;
        return types[ordinal];
    }

    @Override
    public void setPickupFilterType(PickupFilterType type) {
        if (type == null) type = PickupFilterType.ALLOW;
        ItemNBTHelpers.setInt(upgrade, PICKUP_FILTER_TYPE_TAG, type.ordinal());
        save();
    }

    @Override
    public boolean canPickup(ItemStack stack) {
        if (!isEnabled()) return false;

        PickupFilterType filterType = getPickupFilterType();

        if (filterType == PickupFilterType.STORAGE) {
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
            return matchesConfiguredFilter(stack);
        }

        if (!hasAnyFilterItem()) return true;
        boolean matchesFilter = matchesConfiguredFilter(stack);
        return switch (filterType) {
            case ALLOW -> matchesFilter;
            case BLOCK -> !matchesFilter;
            case STORAGE -> matchesFilter;
        };
    }

    private boolean hasAnyFilterItem() {
        BaseItemStackHandler filterItems = getFilterItems();
        for (int i = 0; i < filterItems.getSlots(); i++) {
            if (filterItems.getStackInSlot(i) != null) return true;
        }
        return false;
    }

    private boolean matchesConfiguredFilter(ItemStack stack) {
        return switch (getMatchType()) {
            case ITEM -> matchesItemFilter(stack);
            case MOD -> matchesModFilter(stack);
            case ORE_DICT -> matchesOreDictFilter(stack);
        };
    }

    private boolean matchesItemFilter(ItemStack stack) {
        BaseItemStackHandler filterItems = getFilterItems();
        for (int i = 0; i < filterItems.getSlots(); i++) {
            ItemStack filterStack = filterItems.getStackInSlot(i);
            if (filterStack == null || filterStack.getItem() != stack.getItem()) continue;
            if (matchItemInfo(stack, filterStack)) return true;
        }
        return false;
    }

    private boolean matchesModFilter(ItemStack stack) {
        String stackMod = getModID(stack.getItem());
        BaseItemStackHandler filterItems = getFilterItems();
        for (int i = 0; i < filterItems.getSlots(); i++) {
            ItemStack filterStack = filterItems.getStackInSlot(i);
            if (filterStack == null || filterStack.getItem() == null) continue;
            if (stackMod.equals(getModID(filterStack.getItem()))) return true;
        }
        return false;
    }

    private boolean matchesOreDictFilter(ItemStack stack) {
        List<String> entries = getOreDictEntries();
        if (entries.isEmpty()) return false;

        List<String> stackOreDicts = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(stack)) {
            stackOreDicts.add(OreDictionary.getOreName(id));
        }

        if (isMatchAllOreDicts()) {
            for (String entry : entries) {
                boolean found = stackOreDicts.stream().anyMatch(name -> name.matches(entry));
                if (!found) return false;
            }
            return true;
        }

        for (String entry : entries) {
            if (stackOreDicts.stream().anyMatch(name -> name.matches(entry))) {
                return true;
            }
        }
        return false;
    }
}
