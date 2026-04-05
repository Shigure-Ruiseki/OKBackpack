package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.ISlotModifiable;
import ruiseki.okbackpack.api.wrapper.ISmeltingUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public abstract class AdvancedSmeltingUpgradeWrapperBase extends AdvancedUpgradeWrapper
    implements ISmeltingUpgrade, ISlotModifiable {

    protected BaseItemStackHandler smeltingInventory;
    protected BaseItemStackHandler fuelFilterHandler;

    public AdvancedSmeltingUpgradeWrapperBase(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);

        // Restore filter size to 16 for advanced smelting upgrades
        this.handler = new BaseItemStackHandler(16) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(IBasicFilterable.FILTER_ITEMS_TAG, this.serializeNBT());
                storage.markDirty();
            }
        };
        NBTTagCompound filtersTag = ItemNBTHelpers.getCompound(upgrade, FILTER_ITEMS_TAG, false);
        if (filtersTag != null) handler.deserializeNBT(filtersTag);

        // Fuel filter (4 phantom slots)
        this.fuelFilterHandler = new BaseItemStackHandler(4) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(FUEL_FILTER_TAG, this.serializeNBT());
                storage.markDirty();
            }
        };
        NBTTagCompound fuelFilterTag = ItemNBTHelpers.getCompound(upgrade, FUEL_FILTER_TAG, false);
        if (fuelFilterTag != null) fuelFilterHandler.deserializeNBT(fuelFilterTag);

        this.smeltingInventory = new BaseItemStackHandler(3) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(STORAGE_TAG, this.serializeNBT());
                storage.markDirty();
            }
        };
        NBTTagCompound invTag = ItemNBTHelpers.getCompound(upgrade, STORAGE_TAG, false);
        if (invTag != null) smeltingInventory.deserializeNBT(invTag);
    }

    @Override
    public BaseItemStackHandler getStorage() {
        return smeltingInventory;
    }

    @Override
    public int getTotalCookTime() {
        return 200;
    }

    @Override
    public int getCookTime() {
        return ItemNBTHelpers.getInt(upgrade, COOK_TIME_TAG, 0);
    }

    @Override
    public void setCookTime(int progress) {
        ItemNBTHelpers.setInt(upgrade, COOK_TIME_TAG, progress);
    }

    @Override
    public int getBurnTime() {
        return ItemNBTHelpers.getInt(upgrade, BURN_TIME_TAG, 0);
    }

    @Override
    public void setBurnTime(int progress) {
        ItemNBTHelpers.setInt(upgrade, BURN_TIME_TAG, progress);
    }

    @Override
    public int getTotalBurnTime() {
        return ItemNBTHelpers.getInt(upgrade, BURN_TIME_TOTAL_TAG, 0);
    }

    @Override
    public void setTotalBurnTime(int total) {
        ItemNBTHelpers.setInt(upgrade, BURN_TIME_TOTAL_TAG, total);
    }

    @Override
    public boolean isBurning() {
        return getBurnTime() > 0;
    }

    @Override
    public boolean checkFilter(ItemStack check) {
        return super.checkFilter(check);
    }

    @Override
    public BaseItemStackHandler getFuelFilterItems() {
        return fuelFilterHandler;
    }

    @Override
    public boolean checkFuelFilter(ItemStack stack) {
        if (stack == null) return false;
        boolean hasAnyFilter = false;
        for (int i = 0; i < fuelFilterHandler.getSlots(); i++) {
            ItemStack filterStack = fuelFilterHandler.getStackInSlot(i);
            if (filterStack != null) {
                hasAnyFilter = true;
                if (filterStack.getItem() == stack.getItem()) return true;
            }
        }
        return !hasAnyFilter;
    }

    @Override
    public boolean canAddUpgrade(int slot, ItemStack stack) {
        if (stack == null) return true;
        UpgradeWrapperBase candidate = UpgradeWrapperFactory.createWrapper(stack, storage);
        return !(candidate instanceof ISmeltingUpgrade);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.auto_smelting_settings";
    }

    protected ItemStack getInput() {
        return smeltingInventory.getStackInSlot(0);
    }

    protected void setInput(ItemStack stack) {
        smeltingInventory.setStackInSlot(0, stack);
    }

    protected ItemStack getFuel() {
        return smeltingInventory.getStackInSlot(1);
    }

    protected void setFuel(ItemStack stack) {
        smeltingInventory.setStackInSlot(1, stack);
    }

    protected ItemStack getOutput() {
        return smeltingInventory.getStackInSlot(2);
    }

    protected void setOutput(ItemStack stack) {
        smeltingInventory.setStackInSlot(2, stack);
    }

    protected boolean doAutoSmeltTick() {
        if (!isEnabled()) return false;
        boolean changed = false;

        // Auto-pull input from backpack
        ItemStack input = getInput();
        if (input == null || input.stackSize < input.getMaxStackSize()) {
            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if (stack == null || stack.stackSize <= 0) continue;
                if (!checkFilter(stack)) continue;
                ItemStack result = getSmeltingResult(stack);
                if (result == null) continue;

                if (input == null) {
                    ItemStack pulled = stack.copy();
                    pulled.stackSize = 1;
                    setInput(pulled);
                    stack.stackSize--;
                    changed = true;
                    if (stack.stackSize <= 0) {
                        storage.setStackInSlot(i, null);
                    } else {
                        storage.setStackInSlot(i, stack);
                    }
                    input = getInput();
                    markDirty();
                    break;
                } else if (ItemHandlerHelper.canItemStacksStack(input, stack)) {
                    int space = input.getMaxStackSize() - input.stackSize;
                    int take = Math.min(space, stack.stackSize);
                    if (take > 0) {
                        input.stackSize += take;
                        setInput(input);
                        stack.stackSize -= take;
                        changed = true;
                        if (stack.stackSize <= 0) {
                            storage.setStackInSlot(i, null);
                        } else {
                            storage.setStackInSlot(i, stack);
                        }
                        markDirty();
                        break;
                    }
                }
            }
        }

        // Auto-pull fuel from backpack
        ItemStack fuel = getFuel();
        if (fuel == null || fuel.stackSize < fuel.getMaxStackSize()) {
            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if (stack == null || stack.stackSize <= 0) continue;
                if (!checkFuelFilter(stack)) continue;
                int burnTime = TileEntityFurnace.getItemBurnTime(stack);
                if (burnTime <= 0) continue;

                if (fuel == null) {
                    ItemStack pulled = stack.copy();
                    pulled.stackSize = 1;
                    setFuel(pulled);
                    stack.stackSize--;
                    changed = true;
                    if (stack.stackSize <= 0) {
                        storage.setStackInSlot(i, null);
                    } else {
                        storage.setStackInSlot(i, stack);
                    }
                    fuel = getFuel();
                    markDirty();
                    break;
                } else if (ItemHandlerHelper.canItemStacksStack(fuel, stack)) {
                    int space = fuel.getMaxStackSize() - fuel.stackSize;
                    int take = Math.min(space, stack.stackSize);
                    if (take > 0) {
                        fuel.stackSize += take;
                        setFuel(fuel);
                        stack.stackSize -= take;
                        changed = true;
                        if (stack.stackSize <= 0) {
                            storage.setStackInSlot(i, null);
                        } else {
                            storage.setStackInSlot(i, stack);
                        }
                        markDirty();
                        break;
                    }
                }
            }
        }

        // Auto-push output to backpack
        ItemStack output = getOutput();
        if (output != null && output.stackSize > 0) {
            ItemStack remaining = tryInsertToBackpack(output);
            int remainingSize = remaining == null ? 0 : remaining.stackSize;
            if (remainingSize != output.stackSize) {
                setOutput(remaining);
                changed = true;
            }
        }

        // Do smelting tick
        return doSmeltTick() || changed;
    }

    protected boolean doSmeltTick() {
        boolean dirty = false;
        int fuelProgress = getBurnTime();
        int smeltProgress = getCookTime();
        int fuelTotal = getTotalBurnTime();

        if (fuelProgress > 0) {
            fuelProgress--;
            setBurnTime(fuelProgress);
        }

        ItemStack input = getInput();
        if (input != null) {
            ItemStack result = getSmeltingResult(input);
            if (result != null) {
                ItemStack output = getOutput();
                boolean canOutput = output == null || (ItemHandlerHelper.canItemStacksStack(output, result)
                    && output.stackSize + result.stackSize <= output.getMaxStackSize());

                if (canOutput) {
                    if (fuelProgress <= 0) {
                        ItemStack fuel = getFuel();
                        if (fuel != null) {
                            int burnTime = TileEntityFurnace.getItemBurnTime(fuel);
                            if (burnTime > 0) {
                                fuelProgress = burnTime;
                                fuelTotal = burnTime;
                                setBurnTime(fuelProgress);
                                setTotalBurnTime(fuelTotal);

                                fuel.stackSize--;
                                if (fuel.stackSize <= 0 && fuel.getItem() != null) {
                                    ItemStack containerItem = fuel.getItem()
                                        .getContainerItem(fuel);
                                    setFuel(containerItem);
                                } else {
                                    setFuel(fuel);
                                }
                                dirty = true;
                            }
                        }
                    }

                    if (fuelProgress > 0) {
                        smeltProgress++;
                        if (smeltProgress >= getTotalCookTime()) {
                            smeltProgress = 0;
                            if (output == null) {
                                setOutput(result.copy());
                            } else {
                                output.stackSize += result.stackSize;
                                setOutput(output);
                            }
                            input.stackSize--;
                            if (input.stackSize <= 0) {
                                setInput(null);
                            } else {
                                setInput(input);
                            }
                            dirty = true;
                        }
                        setCookTime(smeltProgress);
                    } else if (smeltProgress > 0) {
                        smeltProgress = Math.max(smeltProgress - 2, 0);
                        setCookTime(smeltProgress);
                    }
                }
            } else if (smeltProgress > 0) {
                smeltProgress = Math.max(smeltProgress - 2, 0);
                setCookTime(smeltProgress);
            }
        } else if (smeltProgress > 0) {
            smeltProgress = Math.max(smeltProgress - 2, 0);
            setCookTime(smeltProgress);
        }

        if (dirty) {
            markDirty();
        }
        return dirty;
    }

    protected ItemStack tryInsertToBackpack(ItemStack output) {
        if (output == null) return null;
        return storage.insertItem(output.copy(), false);
    }

    @Override
    public float getProgress() {
        int total = getTotalCookTime();
        int current = getCookTime();
        return total > 0 ? (float) current / total : 0.0f;
    }

    @Override
    public float getBurnProgress() {
        int total = getTotalBurnTime();
        int current = getBurnTime();
        return total > 0 ? (float) current / total : 0.0f;
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        return doAutoSmeltTick();
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        return doAutoSmeltTick();
    }
}
