package ruiseki.okstoragecore.upgrades;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okstoragecore.inventory.InventoryHandler;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;

public class ContentsFilterLogic extends FilterLogic {

    private final Supplier<InventoryHandler> getInventoryHandler;
    private final MemorySettingsCategory memorySettings;

    public ContentsFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount,
        Supplier<InventoryHandler> getInventoryHandler, MemorySettingsCategory memorySettings, String parentTagKey) {
        super(upgrade, saveHandler, filterSlotCount, parentTagKey);
        this.getInventoryHandler = getInventoryHandler;
        this.memorySettings = memorySettings;
    }

    public ContentsFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount,
        Supplier<InventoryHandler> getInventoryHandler, MemorySettingsCategory memorySettings) {
        super(upgrade, saveHandler, filterSlotCount);
        this.getInventoryHandler = getInventoryHandler;
        this.memorySettings = memorySettings;
    }

    public ContentsFilterType getFilterType() {
        if (shouldFilterByStorage()) {
            return ContentsFilterType.STORAGE;
        }
        return isAllowList() ? ContentsFilterType.ALLOW : ContentsFilterType.BLOCK;
    }

    public void setDepositFilterType(ContentsFilterType contentsFilterType) {
        switch (contentsFilterType) {
            case ALLOW -> {
                setFilterByStorage(false);
                setAllowList(true);
            }
            case BLOCK -> {
                setFilterByStorage(false);
                setAllowList(false);
            }
            case STORAGE -> {
                setFilterByStorage(true);
                save();
            }
        }
    }

    @Override
    public boolean matchesFilter(ItemStack stack) {
        if (!shouldFilterByStorage()) {
            return super.matchesFilter(stack);
        }

        for (ItemStackKey filterStack : getInventoryHandler.get()
            .getSlotTracker()
            .getFullStacks()) {
            if (stackMatchesFilter(stack, filterStack.getStack())) {
                return true;
            }
        }
        for (ItemStackKey filterStack : getInventoryHandler.get()
            .getSlotTracker()
            .getPartialStacks()) {
            if (stackMatchesFilter(stack, filterStack.getStack())) {
                return true;
            }
        }
        return memorySettings.matchesFilter(stack);
    }

    private void setFilterByStorage(boolean filterByStorage) {
        NBTHelpers.setBoolean(upgrade, parentTagKey, "filterByStorage", filterByStorage);
        save();
    }

    private boolean shouldFilterByStorage() {
        return NBTHelpers.getBoolean(upgrade, parentTagKey, "filterByStorage")
            .orElse(false);
    }
}
