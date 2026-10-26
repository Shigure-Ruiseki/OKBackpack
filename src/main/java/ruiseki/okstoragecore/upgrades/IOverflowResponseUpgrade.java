package ruiseki.okstoragecore.upgrades;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.ItemHelpers;

public interface IOverflowResponseUpgrade {

    default boolean stackMatchesFilterStack(ItemStack stack, ItemStack filterStack) {
        if (stack.getItem() != filterStack.getItem()) {
            return false;
        }

        if (getFilterLogic().getPrimaryMatch() == PrimaryMatch.ORE_DICT) {
            return true;
        }

        if (getFilterLogic().shouldMatchDurability() && stack.getItemDamage() != filterStack.getItemDamage()) {
            return false;
        }

        return !getFilterLogic().shouldMatchNbt() || ItemHelpers.areItemsEqualIgnoreDurability(stack, filterStack);
    }

    FilterLogic getFilterLogic();

    boolean worksInGui();

    ItemStack onOverflow(ItemStack stack);

    boolean stackMatchesFilter(ItemStack stack);
}
