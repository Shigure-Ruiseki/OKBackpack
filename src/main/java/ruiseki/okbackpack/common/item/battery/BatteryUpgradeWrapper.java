package ruiseki.okbackpack.common.item.battery;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cofh.api.energy.IEnergyContainerItem;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IBatteryUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;
import ruiseki.okcore.helper.LangHelpers;

public class BatteryUpgradeWrapper extends UpgradeWrapperBase implements IBatteryUpgrade {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    public static final String ENERGY_STORED_TAG = "EnergyStored";
    public static final String BATTERY_INV_TAG = "BatteryInv";

    public static final int BASE_ENERGY_PER_SLOT = 15000;
    public static final int BASE_MAX_TRANSFER = 200;

    protected final BaseItemStackHandler batteryInventory;
    public int energyStored;

    public BatteryUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);

        this.batteryInventory = new BaseItemStackHandler(2) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(BATTERY_INV_TAG, this.serializeNBT());
                save();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        NBTTagCompound invTag = ItemNBTHelpers.getCompound(upgrade, BATTERY_INV_TAG, false);
        if (invTag != null) batteryInventory.deserializeNBT(invTag);

        this.energyStored = ItemNBTHelpers.getInt(upgrade, ENERGY_STORED_TAG, 0);
    }

    public static int getEnergyStoredStatic(ItemStack upgrade) {
        return ItemNBTHelpers.getInt(upgrade, ENERGY_STORED_TAG, 0);
    }

    @Override
    public void onAdded() {
        int size = storage.getStackHandler()
            .getVisualSize();
        size -= ItemBatteryUpgrade.SLOTS_NEEDED;
        storage.getStackHandler()
            .setVisualSize(size);
        storage.markDirty();
    }

    @Override
    public void onBeforeRemoved() {
        int size = storage.getStackHandler()
            .getVisualSize();
        size += ItemBatteryUpgrade.SLOTS_NEEDED;
        storage.getStackHandler()
            .setVisualSize(size);
        storage.markDirty();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int maxInOut = getMaxTransfer();
        int ret = Math.min(getMaxEnergyStored() - energyStored, Math.min(maxInOut, maxReceive));
        if (ret <= 0) return 0;
        if (!simulate) {
            energyStored += ret;
            serializeEnergyStored();
        }
        return ret;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int maxInOut = getMaxTransfer();
        int ret = Math.min(energyStored, Math.min(maxInOut, maxExtract));
        if (ret <= 0) return 0;
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
        int slots = storage.getSlots();
        double stackMultiplier = storage.applyStackLimitModifiers();
        return (int) (BASE_ENERGY_PER_SLOT * slots * stackMultiplier);
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public BaseItemStackHandler getStorage() {
        return batteryInventory;
    }

    @Override
    public float getChargeRatio() {
        int max = getMaxEnergyStored();
        return max > 0 ? (float) energyStored / max : 0f;
    }

    @Override
    public int getMaxTransfer() {
        return getMaxTransferInternal();
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.battery_settings";
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        return doTick();
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        return doTick();
    }

    public boolean doTick() {
        boolean dirty = false;

        // Input slot: extract energy from item → battery
        if (energyStored < getMaxEnergyStored()) {
            ItemStack inputStack = batteryInventory.getStackInSlot(INPUT_SLOT);
            if (inputStack != null && inputStack.getItem() instanceof IEnergyContainerItem energyItem) {
                dirty |= receiveFromItem(inputStack, energyItem);
            }
        }

        // Output slot: transfer energy from battery → item
        if (energyStored > 0) {
            ItemStack outputStack = batteryInventory.getStackInSlot(OUTPUT_SLOT);
            if (outputStack != null && outputStack.getItem() instanceof IEnergyContainerItem energyItem) {
                dirty |= extractToItem(outputStack, energyItem);
            }
        }

        return dirty;
    }

    public boolean receiveFromItem(ItemStack stack, IEnergyContainerItem energyItem) {
        int maxTransfer = getMaxTransfer();
        int canReceive = Math.min(getMaxEnergyStored() - energyStored, maxTransfer);
        if (canReceive <= 0) return false;

        int extracted = energyItem.extractEnergy(stack, canReceive, true);
        if (extracted <= 0) return false;

        extracted = energyItem.extractEnergy(stack, extracted, false);
        if (extracted > 0) {
            energyStored += extracted;
            serializeEnergyStored();
            return true;
        }
        return false;
    }

    public boolean extractToItem(ItemStack stack, IEnergyContainerItem energyItem) {
        int maxTransfer = getMaxTransfer();
        int canExtract = Math.min(energyStored, maxTransfer);
        if (canExtract <= 0) return false;

        int received = energyItem.receiveEnergy(stack, canExtract, true);
        if (received <= 0) return false;

        received = energyItem.receiveEnergy(stack, received, false);
        if (received > 0) {
            energyStored -= received;
            serializeEnergyStored();
            return true;
        }
        return false;
    }

    public int getMaxTransferInternal() {
        int slots = storage.getSlots();
        double stackMultiplier = storage.applyStackLimitModifiers();
        return (int) (BASE_MAX_TRANSFER * (slots / 9.0) * stackMultiplier);
    }

    public void serializeEnergyStored() {
        ItemNBTHelpers.setInt(upgrade, ENERGY_STORED_TAG, energyStored);
        save();
    }

    public boolean isValidInputItem(ItemStack stack) {
        if (stack == null) return false;
        if (!(stack.getItem() instanceof IEnergyContainerItem energyItem)) return false;
        return energyItem.extractEnergy(stack, 1, true) > 0;
    }

    public boolean isValidOutputItem(ItemStack stack) {
        if (stack == null) return false;
        if (!(stack.getItem() instanceof IEnergyContainerItem energyItem)) return false;
        return energyItem.receiveEnergy(stack, 1, true) > 0;
    }

    @Override
    public List<String> getTooltipLines() {
        return Collections
            .singletonList("\u00a7c" + LangHelpers.localize("tooltip.backpack.contents.energy", energyStored));
    }
}
