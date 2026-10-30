package ruiseki.okstoragecore.upgrades;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okstoragecore.inventory.FilterItemStackHandler;

public class FilterLogic extends FilterLogicBase {

    private final int filterSlotCount;
    private final Predicate<ItemStack> isItemValid;
    private ObservableFilterItemStackHandler filterHandler = null;
    private boolean emptyAllowListMatchesEverything = false;

    public FilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount) {
        this(upgrade, saveHandler, filterSlotCount, s -> true, "");
    }

    public FilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, String parentTagKey) {
        this(upgrade, saveHandler, filterSlotCount, s -> true, parentTagKey);
    }

    public FilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount,
        Predicate<ItemStack> isItemValid) {
        this(upgrade, saveHandler, filterSlotCount, isItemValid, "");
    }

    public FilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount,
        Predicate<ItemStack> isItemValid, String parentTagKey) {
        super(upgrade, saveHandler, parentTagKey);
        this.filterSlotCount = filterSlotCount;
        this.isItemValid = isItemValid;
    }

    public ObservableFilterItemStackHandler getFilterHandler() {
        if (filterHandler == null) {
            filterHandler = new ObservableFilterItemStackHandler();
            NBTHelpers.getCompoundTag(upgrade, parentTagKey, "filters")
                .ifPresent(filterHandler::deserializeNBT);
        }

        return filterHandler;
    }

    public boolean matchesFilter(ItemStack stack) {
        if (isAllowList()) {
            if (getPrimaryMatch() == PrimaryMatch.ORE_DICT) {
                return isOreDictMatch(stack);
            } else {
                return (getFilterHandler().hasOnlyEmptyFilters() && emptyAllowListMatchesEverything)
                    || InventoryHelpers.iterate(
                        getFilterHandler(),
                        (slot, filter) -> stackMatchesFilter(stack, filter),
                        () -> false,
                        returnValue -> returnValue);
            }
        } else {
            if (getPrimaryMatch() == PrimaryMatch.ORE_DICT) {
                return !isOreDictMatch(stack);
            } else {
                return getFilterHandler().hasOnlyEmptyFilters() || InventoryHelpers.iterate(
                    getFilterHandler(),
                    (slot, filter) -> !stackMatchesFilter(stack, filter),
                    () -> true,
                    returnValue -> !returnValue);
            }
        }
    }

    private boolean isOreDictMatch(ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return false;
        int[] oreIds = OreDictionary.getOreIDs(stack);
        if (shouldMatchAnyOreDict()) {
            return anyOreDictMatches(oreIds);
        }
        return allOreDictsMatch(oreIds);
    }

    private boolean allOreDictsMatch(int[] oreIds) {
        List<String> entries = getOreDictEntries();
        if (entries.isEmpty()) return false;
        Set<String> oreNames = new HashSet<>();
        for (int oreId : oreIds) {
            oreNames.add(OreDictionary.getOreName(oreId));
        }
        return oreNames.containsAll(entries);
    }

    private boolean anyOreDictMatches(int[] oreIds) {
        List<String> entries = getOreDictEntries();
        if (entries.isEmpty()) return false;
        for (int oreId : oreIds) {
            if (entries.contains(OreDictionary.getOreName(oreId))) {
                return true;
            }
        }
        return false;
    }

    public class ObservableFilterItemStackHandler extends FilterItemStackHandler {

        private IntConsumer onSlotChange = s -> {};

        public ObservableFilterItemStackHandler() {
            super(filterSlotCount);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            NBTHelpers.setCompoundNBT(upgrade, parentTagKey, "filters", serializeNBT());
            save();
            onSlotChange.accept(slot);
        }

        public void setOnSlotChange(IntConsumer onSlotChange) {
            this.onSlotChange = onSlotChange;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            setSize(filterSlotCount);
            NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
                int slot = itemTags.getInteger("Slot");

                if (slot >= 0 && slot < stacks.size()) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(itemTags);
                    stacks.set(slot, stack);
                }
            }
            onLoad();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return ItemHelpers.isEmpty(stack) || (doesNotContain(stack) && isItemValid.test(stack));
        }

        private boolean doesNotContain(ItemStack stack) {
            return !InventoryHelpers.hasItem(this, s -> ItemHandlerHelpers.canItemStacksStack(s, stack));
        }
    }
}
