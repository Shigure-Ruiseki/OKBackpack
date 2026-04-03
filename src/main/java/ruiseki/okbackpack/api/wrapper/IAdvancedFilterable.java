package ruiseki.okbackpack.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.helper.ItemStackHelpers;

public interface IAdvancedFilterable extends IBasicFilterable {

    String MATCH_TYPE_TAG = "MatchType";
    String IGNORE_DURABILITY_TAG = "IgnoreDurability";
    String IGNORE_NBT_TAG = "IgnoreNbt";
    String ORE_DICT_LIST_TAG = "OreDict";
    String ORE_DICT_ITEMS_TAG = "OreDictItem";

    UpgradeItemStackHandler getOreDictItem();

    MatchType getMatchType();

    void setMatchType(MatchType matchType);

    List<String> getOreDictEntries();

    void setOreDictEntries(List<String> entries);

    boolean isIgnoreDurability();

    void setIgnoreDurability(boolean ignore);

    boolean isIgnoreNBT();

    void setIgnoreNBT(boolean ignore);

    default boolean checkFilter(ItemStack stack) {
        switch (getMatchType()) {
            case ITEM:
                return matchItem(stack);
            case MOD:
                return matchMod(stack);
            case ORE_DICT:
                return matchOreDict(stack);
            default:
                return false;
        }
    }

    default boolean matchItem(ItemStack stack) {
        boolean[] filterResult = new boolean[16];
        UpgradeItemStackHandler filterItems = getFilterItems();

        for (int i = 0; i < filterItems.getSlots(); i++) {
            ItemStack filterStack = filterItems.getStackInSlot(i);
            if (filterStack == null || filterStack.getItem() != stack.getItem()) {
                continue;
            }
            filterResult[i] = matchItemInfo(stack, filterStack);
        }

        switch (getFilterType()) {
            case WHITELIST:
                for (boolean b : filterResult) if (b) {
                    return true;
                }
                return false;
            case BLACKLIST:
                for (boolean b : filterResult) if (b) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    default boolean matchMod(ItemStack stack) {
        boolean[] filterResult = new boolean[16];
        UpgradeItemStackHandler filterItems = getFilterItems();

        for (int i = 0; i < filterItems.getSlots(); i++) {
            ItemStack filterStack = filterItems.getStackInSlot(i);
            if (filterStack == null || filterStack.getItem() == null) {
                filterResult[i] = false;
                continue;
            }

            String stackMod = getModID(stack.getItem());
            String filterMod = getModID(filterStack.getItem());

            filterResult[i] = stackMod.equals(filterMod);
        }

        switch (getFilterType()) {
            case WHITELIST:
                for (boolean b : filterResult) if (b) {
                    return true;
                }
                return false;
            case BLACKLIST:
                for (boolean b : filterResult) if (b) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    default String getModID(Item item) {
        String unlocalized = item.getUnlocalizedName();
        if (unlocalized.contains(":")) {
            return unlocalized.split(":")[0];
        }
        return "minecraft";
    }

    default boolean matchOreDict(ItemStack stack) {
        List<String> stackOreDicts = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(stack)) {
            stackOreDicts.add(OreDictionary.getOreName(id));
        }

        for (String entry : getOreDictEntries()) {
            if (stackOreDicts.stream()
                .anyMatch(s -> s.matches(entry))) {
                return getFilterType() == IBasicFilterable.FilterType.WHITELIST;
            }
        }

        return getFilterType() == FilterType.BLACKLIST;
    }

    default boolean matchItemInfo(ItemStack stack, ItemStack filterStack) {
        if (filterStack == null) {
            return false;
        }

        boolean flag;
        if (isIgnoreDurability()) {
            flag = ItemStackHelpers.areItemsEqualIgnoreDurability(filterStack, stack);
        } else {
            flag = filterStack.isItemEqual(stack);
        }

        if (!isIgnoreNBT()) {
            flag = flag && ((filterStack.getTagCompound() == null && stack.getTagCompound() == null)
                || (filterStack.getTagCompound() != null && filterStack.getTagCompound()
                    .equals(stack.getTagCompound())));
        }

        return flag;
    }

    enum MatchType {
        ITEM,
        MOD,
        ORE_DICT
    }
}
