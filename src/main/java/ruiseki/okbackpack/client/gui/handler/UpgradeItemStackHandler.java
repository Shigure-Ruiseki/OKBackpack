package ruiseki.okbackpack.client.gui.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;

public class UpgradeItemStackHandler extends BaseItemStackHandler {

    private final IStorageWrapper storage;

    @Nullable
    private Runnable refreshCallBack = null;
    private final Map<Integer, IUpgradeWrapper> slotWrappers = new LinkedHashMap<>();
    protected boolean justSavingNbtChange = false;
    private boolean wrappersInitialized = false;

    public UpgradeItemStackHandler(int size, IStorageWrapper storage) {
        super(size);
        this.storage = storage;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack == null) return true;
        if (!(stack.getItem() instanceof IUpgradeItem<?>upgradeItem)) return false;
        return upgradeItem.canAddUpgradeTo(storage, stack, slot)
            .isSuccessful();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (!justSavingNbtChange) {
            refreshUpgradeWrappers();
        }
    }

    private void initializeWrappers() {
        if (wrappersInitialized) {
            return;
        }
        wrappersInitialized = true;
        slotWrappers.clear();

        BackpackInventoryHelpers.iterate(this, (slot, upgrade) -> {
            if (upgrade == null || !(upgrade.getItem() instanceof IUpgradeItem<?>item)) {
                return;
            }
            IUpgradeWrapper wrapper = item.createWrapper(upgrade, storage, upgradeStack -> {
                justSavingNbtChange = true;
                setStackInSlot(slot, upgradeStack);
                justSavingNbtChange = false;
            });
            slotWrappers.put(slot, wrapper);
        });
    }

    @Override
    public @Nullable ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!isVisualSlot(slot)) return stack;
        ItemStack before = getStackInSlot(slot);
        ItemStack result = super.insertItem(slot, stack, simulate);
        if (!simulate && before == null && getStackInSlot(slot) != null) {
            onUpgradeAdded(slot);
        }

        return result;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (!isVisualSlot(slot)) return;
        ItemStack originalStack = getStackInSlot(slot);
        boolean itemsDiffer = !ItemStack.areItemStacksEqual(originalStack, stack);

        if (itemsDiffer) {
            // Fire onBeforeRemoved BEFORE changing the stack
            Map<Integer, IUpgradeWrapper> wrappers = getSlotWrappers();
            if (wrappers.containsKey(slot)) {
                wrappers.get(slot)
                    .onBeforeRemoved();
            }

            // Suppress premature refresh during super.setStackInSlot
            // (onContentsChanged would trigger refreshUpgradeWrappers before lifecycle hooks complete)
            boolean wasJustSaving = justSavingNbtChange;
            justSavingNbtChange = true;
            super.setStackInSlot(slot, stack);
            justSavingNbtChange = wasJustSaving;

            // Force wrapper re-creation so onUpgradeAdded sees the new stack
            wrappersInitialized = false;
            onUpgradeAdded(slot);

            // Invalidate wrappers so next access re-creates them with correct state
            wrappersInitialized = false;
        } else {
            super.setStackInSlot(slot, stack);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isVisualSlot(slot)) return null;
        if (!simulate) {
            ItemStack slotStack = getStackInSlot(slot);
            if (slotStack != null && amount == 1) {
                Map<Integer, IUpgradeWrapper> wrappers = getSlotWrappers();
                if (wrappers.containsKey(slot)) {
                    wrappers.get(slot)
                        .onBeforeRemoved();
                }
            }
        }
        return super.extractItem(slot, amount, simulate);
    }

    private void onUpgradeAdded(int slot) {
        Map<Integer, IUpgradeWrapper> wrappers = getSlotWrappers();
        if (wrappers.containsKey(slot)) {
            wrappers.get(slot)
                .onAdded();
        }
    }

    public <T> List<T> getListOfWrappersThatImplement(Class<T> uc) {
        List<T> ret = new ArrayList<>();
        for (IUpgradeWrapper wrapper : slotWrappers.values()) {
            if (wrapper instanceof IToggleable toggleable && !toggleable.isEnabled()) continue;
            if (uc.isInstance(wrapper)) {
                // noinspection unchecked
                ret.add((T) wrapper);
            }
        }
        return ret;
    }

    public Map<Integer, IUpgradeWrapper> getSlotWrappers() {
        initializeWrappers();
        return slotWrappers;
    }

    @Nullable
    public IUpgradeWrapper getWrapperInSlot(int slot) {
        return getSlotWrappers().get(slot);
    }

    public void refreshUpgradeWrappers() {
        if (!wrappersInitialized) {
            return;
        }

        wrappersInitialized = false;
        if (refreshCallBack != null) {
            refreshCallBack.run();
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}
