package ruiseki.okbackpack.common.item.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.api.wrapper.IBatteryUpgrade;
import ruiseki.okbackpack.api.wrapper.IInfinityUpgrade;
import ruiseki.okbackpack.api.wrapper.IStackSizeUpgrade;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okbackpack.common.item.battery.BatteryUpgradeWrapper;
import ruiseki.okbackpack.common.item.tank.ItemTankUpgrade;
import ruiseki.okbackpack.common.item.tank.TankUpgradeWrapper;

public class StackUpgradeWrapper extends UpgradeWrapperBase implements IStackSizeUpgrade {

    private static class StackModifierState {

        public double additiveTotal;
        public double downgradeProduct = 1;
        public boolean hasAdditive;
    }

    public StackUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean canAddUpgrade(int slot, ItemStack stack) {
        return true; // luôn có thể thêm
    }

    @Override
    public boolean canRemoveUpgrade(int slotIndex) {
        return getRemoveUpgradeResult(slotIndex).isSuccessful();
    }

    @Override
    public UpgradeSlotChangeResult getRemoveUpgradeResult(int slotIndex) {

        if (!storage.gatherCapabilityUpgrades(IInfinityUpgrade.class)
            .isEmpty()) {
            return UpgradeSlotChangeResult.success();
        }

        double totalMultiplier = toEffectiveMultiplier(calculateStateExcluding(slotIndex));

        List<Integer> conflictSlots = new ArrayList<>();
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack == null) continue;

            double rawLimit = stack.getMaxStackSize() * totalMultiplier;
            long newLimit = rawLimit >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (long) Math.ceil(rawLimit);

            if (stack.stackSize > newLimit) {
                conflictSlots.add(i);
            }
        }

        if (!conflictSlots.isEmpty()) {
            return UpgradeSlotChangeResult.failStackLowMultiplier(
                conflictSlots.stream()
                    .mapToInt(Integer::intValue)
                    .toArray(),
                ItemStackUpgrade.formatMultiplier(totalMultiplier));
        }

        UpgradeSlotChangeResult storageResult = checkStorageUpgradeCapacity(totalMultiplier);
        if (!storageResult.isSuccessful()) return storageResult;

        return UpgradeSlotChangeResult.success();
    }

    @Override
    public boolean canReplaceUpgrade(int slotIndex, ItemStack replacement) {
        return getReplaceUpgradeResult(slotIndex, replacement).isSuccessful();
    }

    @Override
    public UpgradeSlotChangeResult getReplaceUpgradeResult(int slotIndex, ItemStack replacement) {
        if (replacement == null) return UpgradeSlotChangeResult.success();

        if (!storage.gatherCapabilityUpgrades(IInfinityUpgrade.class)
            .isEmpty()) {
            return UpgradeSlotChangeResult.success();
        }

        StackModifierState state = calculateStateExcluding(slotIndex);

        // Apply replacement by type: upgrades are additive, downgrades are multiplicative.
        if (replacement.getItem() instanceof ItemStackUpgrade) {
            double replacementMultiplier = ItemStackUpgrade.multiplier(replacement);
            if (ItemStackUpgrade.isDowngrade(replacement)) {
                state.downgradeProduct *= replacementMultiplier;
            } else {
                state.additiveTotal += replacementMultiplier;
                state.hasAdditive = true;
            }
        }

        double totalMultiplier = toEffectiveMultiplier(state);

        List<Integer> conflictSlots = new ArrayList<>();
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack == null) continue;

            double rawLimit = stack.getMaxStackSize() * totalMultiplier;
            long maxAllowed = rawLimit >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (long) Math.ceil(rawLimit);

            if (stack.stackSize > maxAllowed) {
                conflictSlots.add(i);
            }
        }

        if (!conflictSlots.isEmpty()) {
            return UpgradeSlotChangeResult.failStackLowMultiplier(
                conflictSlots.stream()
                    .mapToInt(Integer::intValue)
                    .toArray(),
                ItemStackUpgrade.formatMultiplier(totalMultiplier));
        }

        UpgradeSlotChangeResult storageResult = checkStorageUpgradeCapacity(totalMultiplier);
        if (!storageResult.isSuccessful()) return storageResult;

        return UpgradeSlotChangeResult.success();
    }

    private UpgradeSlotChangeResult checkStorageUpgradeCapacity(double newMultiplier) {
        // Check battery energy against new capacity
        for (var entry : storage.gatherCapabilityUpgrades(IBatteryUpgrade.class)
            .entrySet()) {
            IBatteryUpgrade battery = entry.getValue();
            int newMaxEnergy = (int) (BatteryUpgradeWrapper.BASE_ENERGY_PER_SLOT * storage.getStackHandler()
                .getSlots() * newMultiplier);
            if (battery.getEnergyStored() > newMaxEnergy) {
                return UpgradeSlotChangeResult.failStorageCapacityLow(
                    new int[] { entry.getKey() },
                    ItemStackUpgrade.formatMultiplier(newMultiplier));
            }
        }

        // Check tank fluid against new capacity
        for (var entry : storage.gatherCapabilityUpgrades(ITankUpgrade.class)
            .entrySet()) {
            ITankUpgrade tank = entry.getValue();
            int newMaxFluid = (int) (ItemTankUpgrade.SLOTS_NEEDED * TankUpgradeWrapper.BASE_CAPACITY_PER_SLOT
                * newMultiplier);
            if (tank.getContents() != null && tank.getContents().amount > newMaxFluid) {
                return UpgradeSlotChangeResult.failStorageCapacityLow(
                    new int[] { entry.getKey() },
                    ItemStackUpgrade.formatMultiplier(newMultiplier));
            }
        }

        return UpgradeSlotChangeResult.success();
    }

    private StackModifierState calculateStateExcluding(int excludedSlot) {
        StackModifierState state = new StackModifierState();

        for (var entry : storage.gatherCapabilityUpgrades(IStackSizeUpgrade.class)
            .entrySet()) {
            if (entry.getKey() == excludedSlot) continue;

            ItemStack stack = storage.getUpgradeHandler()
                .getStackInSlot(entry.getKey());
            if (stack == null) continue;

            IStackSizeUpgrade modifier = entry.getValue();
            if (modifier.isDowngrade()) {
                state.downgradeProduct *= modifier.getMultiplier();
            } else {
                state.additiveTotal += modifier.getMultiplier();
                state.hasAdditive = true;
            }
        }

        return state;
    }

    private double toEffectiveMultiplier(StackModifierState state) {
        double total = (state.hasAdditive ? state.additiveTotal : 1) * state.downgradeProduct;
        return total <= 0 ? 1.0 : total;
    }

    @Override
    public double getMultiplier() {
        return ItemStackUpgrade.multiplier(upgrade);
    }

    @Override
    public boolean isDowngrade() {
        return ItemStackUpgrade.isDowngrade(upgrade);
    }
}
