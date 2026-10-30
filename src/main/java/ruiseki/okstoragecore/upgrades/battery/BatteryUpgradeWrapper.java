package ruiseki.okstoragecore.upgrades.battery;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import cofh.api.energy.IEnergyStorage;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.energy.capability.CapabilityEnergy;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IStackableContentsUpgrade;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class BatteryUpgradeWrapper extends UpgradeWrapperBase<BatteryUpgradeWrapper, ItemBatteryUpgrade>
    implements IEnergyStorage, ITickableUpgrade, IStackableContentsUpgrade, IStorageUpgrade {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final String ENERGY_STORED_TAG = "energyStored";
    private final ItemStackHandler inventory;
    private int energyStored;

    protected BatteryUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        this.inventory = new ItemStackHandler(10) {

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                NBTHelpers.setCompoundTag(upgrade, "inventory", this.serializeNBT());
                save();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == INPUT_SLOT) {
                    return isValidInputItem(stack);
                } else if (slot == OUTPUT_SLOT) {
                    return isValidOutputItem(stack);
                }
                return false;
            }

            private boolean isValidInputItem(ItemStack stack) {
                return isValidEnergyItem(stack, false);
            }

            private boolean isValidOutputItem(ItemStack stack) {
                return isValidEnergyItem(stack, true);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        NBTHelpers.getCompoundTag(upgrade, "inventory")
            .ifPresent(this.inventory::deserializeNBT);
        energyStored = getEnergyStored(upgrade);
    }

    public static int getEnergyStored(ItemStack upgrade) {
        return NBTHelpers.getInteger(upgrade, ENERGY_STORED_TAG)
            .orElse(0);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return innerReceiveEnergy(maxReceive, simulate);
    }

    private int innerReceiveEnergy(int maxReceive, boolean simulate) {
        int ret = Math.min(getMaxEnergyStored() - energyStored, Math.min(getMaxInOut(), maxReceive));
        if (!simulate) {
            energyStored += ret;
            serializeEnergyStored();
        }
        return ret;
    }

    private void serializeEnergyStored() {
        NBTHelpers.setInteger(upgrade, ENERGY_STORED_TAG, energyStored);
        save();
        // forceUpdateBatteryRenderInfo();
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return innerExtractEnergy(maxExtract, simulate);
    }

    private int innerExtractEnergy(int maxExtract, boolean simulate) {
        int ret = Math.min(energyStored, Math.min(getMaxInOut(), maxExtract));

        if (!simulate) {
            energyStored -= ret;
            serializeEnergyStored();
        }
        return ret;
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return upgradeItem.getMaxEnergyStored(storageWrapper);
    }

    private int getMaxInOut() {
        int stackMultiplier = upgradeItem.getAdjustedStackMultiplier(storageWrapper);
        int baseInOut = upgradeItem.getBatteryUpgradeConfig()
            .getMaxInputOutput() * storageWrapper.getNumberOfSlotRows();
        return stackMultiplier > Integer.MAX_VALUE / baseInOut ? Integer.MAX_VALUE : baseInOut * stackMultiplier;
    }

    private boolean isValidEnergyItem(ItemStack stack, boolean isOutput) {
        return CapabilityHelpers.getCapability(stack, CapabilityEnergy.ENERGY)
            .map(energyStorage -> isOutput || energyStorage.getEnergyStored() > 0)
            .orElse(false);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (energyStored < getMaxEnergyStored()) {
            CapabilityHelpers.getCapability(inventory.getStackInSlot(INPUT_SLOT), CapabilityEnergy.ENERGY)
                .ifPresent(this::receiveFromStorage);
        }

        if (energyStored > 0) {
            CapabilityHelpers.getCapability(inventory.getStackInSlot(OUTPUT_SLOT), CapabilityEnergy.ENERGY)
                .ifPresent(this::extractToStorage);
        }
    }

    private void extractToStorage(IEnergyStorage energyStorage) {
        int toExtract = innerExtractEnergy(getMaxInOut(), true);
        if (toExtract > 0) {
            toExtract = energyStorage.receiveEnergy(toExtract, true);
            if (toExtract > 0) {
                energyStorage.receiveEnergy(toExtract, false);
                innerExtractEnergy(toExtract, false);
            }
        }
    }

    private void receiveFromStorage(IEnergyStorage energyStorage) {
        int toReceive = innerReceiveEnergy(getMaxInOut(), true);
        if (toReceive > 0) {
            toReceive = energyStorage.extractEnergy(toReceive, true);
            if (toReceive > 0) {
                energyStorage.extractEnergy(toReceive, false);
                innerReceiveEnergy(toReceive, false);
            }
        }
    }

    @Override
    public IItemHandlerModifiable getStorage() {
        return this.inventory;
    }

    @Override
    public int getMinimumMultiplierRequired() {
        return (int) Math.ceil((float) energyStored / upgradeItem.getMaxEnergyBase(storageWrapper));
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }
}
