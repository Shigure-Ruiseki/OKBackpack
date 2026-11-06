package ruiseki.backpack.upgrades.inception;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.backpack.ItemBackpack;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.InventoryHandler;

public class SubBackpacksHandler {

    private final Map<Integer, IStorageWrapper> subBackpacks = new LinkedHashMap<>();

    private final InventoryHandler inventoryHandler;
    private final Set<Consumer<Collection<IStorageWrapper>>> refreshListeners = new HashSet<>();
    private final Set<Consumer<Collection<IStorageWrapper>>> beforeRefreshListeners = new HashSet<>();

    public SubBackpacksHandler(InventoryHandler inventoryHandler) {
        this.inventoryHandler = inventoryHandler;
        this.inventoryHandler.addListener(this::onContentsChanged);

        refreshSubBackpacks();
    }

    public void addRefreshListener(Consumer<Collection<IStorageWrapper>> listener) {
        refreshListeners.add(listener);
    }

    public Collection<IStorageWrapper> getSubBackpacks() {
        return subBackpacks.values();
    }

    private void onContentsChanged(int slot) {
        ItemStack stackInSlot = inventoryHandler.getStackInSlot(slot);
        boolean backpackIsInTheSlot = ItemHelpers.hasItem(stackInSlot, ItemBackpack.class);
        boolean backpackWasInTheSlot = subBackpacks.containsKey(slot);
        if (!backpackWasInTheSlot && !backpackIsInTheSlot) {
            return;
        }

        if (backpackWasInTheSlot != backpackIsInTheSlot) {
            notifyAndRefreshSubbackpacks();
        } else {
            LazyOptional<IBackpackWrapper> backpackWrapper = CapabilityHelpers
                .getCapability(stackInSlot, CapabilityBackpackWrapperConfig.CAPABILITY);
            if (backpackWrapper.isPresent() && backpackWrapper.map(w -> w != subBackpacks.get(slot))
                .orElse(false)) {
                notifyAndRefreshSubbackpacks();
            }
        }
    }

    private void notifyAndRefreshSubbackpacks() {
        notifyBeforeRefresh();
        refreshSubBackpacks();
        notifyAfterRefresh();
    }

    private void notifyAfterRefresh() {
        runRefreshListeners(refreshListeners);
    }

    private void runRefreshListeners(Set<Consumer<Collection<IStorageWrapper>>> refreshListeners) {
        for (Consumer<Collection<IStorageWrapper>> refreshListener : refreshListeners) {
            refreshListener.accept(subBackpacks.values());
        }
    }

    private void notifyBeforeRefresh() {
        runRefreshListeners(beforeRefreshListeners);
    }

    private void refreshSubBackpacks() {
        subBackpacks.clear();

        for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
            int finalSlot = slot;
            CapabilityHelpers
                .getCapability(inventoryHandler.getStackInSlot(slot), CapabilityBackpackWrapperConfig.CAPABILITY)
                .ifPresent(wrapper -> subBackpacks.put(finalSlot, wrapper));
        }
    }

    public void addBeforeRefreshListener(Consumer<Collection<IStorageWrapper>> listener) {
        beforeRefreshListeners.add(listener);
    }
}
