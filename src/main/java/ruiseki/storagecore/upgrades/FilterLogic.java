package ruiseki.storagecore.upgrades;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.helper.TagHelpers;
import ruiseki.okcore.tag.TagKey;
import ruiseki.storagecore.inventory.FilterItemStackHandler;

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
            if (getPrimaryMatch() == PrimaryMatch.TAG) {
                return isTagMatch(stack);
            } else {
                return (getFilterHandler().hasOnlyEmptyFilters() && emptyAllowListMatchesEverything)
                    || InventoryHelpers.iterate(
                        getFilterHandler(),
                        (slot, filter) -> stackMatchesFilter(stack, filter),
                        () -> false,
                        returnValue -> returnValue);
            }
        } else {
            if (getPrimaryMatch() == PrimaryMatch.TAG) {
                return !isTagMatch(stack);
            } else {
                return getFilterHandler().hasOnlyEmptyFilters() || InventoryHelpers.iterate(
                    getFilterHandler(),
                    (slot, filter) -> !stackMatchesFilter(stack, filter),
                    () -> true,
                    returnValue -> !returnValue);
            }
        }
    }

    private boolean isTagMatch(ItemStack stack) {
        if (shouldMatchAnyTag()) {
            return anyTagMatches(TagHelpers.getTags(stack));
        }
        return allTagsMatch(TagHelpers.getTags(stack));
    }

    private boolean allTagsMatch(Set<TagKey<Item>> tags) {
        if (tagKeys == null) {
            initTags();
        }
        for (TagKey<Item> tagName : tagKeys) {
            if (!tags.contains(tagName)) {
                return false;
            }
        }
        return true;
    }

    private boolean anyTagMatches(Set<TagKey<Item>> tags) {
        if (tagKeys == null) {
            initTags();
        }
        return tags.stream()
            .anyMatch(t -> tagKeys.contains(t));
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
