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
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
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
            }
        };
        NBTTagCompound fuelFilterTag = ItemNBTHelpers.getCompound(upgrade, FUEL_FILTER_TAG, false);
        if (fuelFilterTag != null) fuelFilterHandler.deserializeNBT(fuelFilterTag);

        this.smeltingInventory = new BaseItemStackHandler(3) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag("SmeltingInv", this.serializeNBT());
                markDirty();
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack == null) return null;
                if (slot == 0 && !checkFilter(stack)) return stack;
                if (slot == 1 && !checkFuelFilter(stack)) return stack;
                if (slot == 2) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
        NBTTagCompound invTag = ItemNBTHelpers.getCompound(upgrade, "SmeltingInv", false);
        if (invTag != null) smeltingInventory.deserializeNBT(invTag);
    }

    @Override
    public BaseItemStackHandler getSmeltingInventory() {
        return smeltingInventory;
    }

    @Override
    public int getSmeltTime() {
        return 200;
    }

    @Override
    public int getSmeltProgress() {
        return ItemNBTHelpers.getInt(upgrade, SMELTING_PROGRESS_TAG, 0);
    }

    @Override
    public void setSmeltProgress(int progress) {
        ItemNBTHelpers.setInt(upgrade, SMELTING_PROGRESS_TAG, progress);
        markDirty();
    }

    @Override
    public int getFuelProgress() {
        return ItemNBTHelpers.getInt(upgrade, SMELTING_FUEL_PROGRESS_TAG, 0);
    }

    @Override
    public void setFuelProgress(int progress) {
        ItemNBTHelpers.setInt(upgrade, SMELTING_FUEL_PROGRESS_TAG, progress);
        markDirty();
    }

    @Override
    public int getFuelTotal() {
        return ItemNBTHelpers.getInt(upgrade, SMELTING_FUEL_TOTAL_TAG, 0);
    }

    @Override
    public void setFuelTotal(int total) {
        ItemNBTHelpers.setInt(upgrade, SMELTING_FUEL_TOTAL_TAG, total);
        markDirty();
    }

    @Override
    public boolean isBurning() {
        return getFuelProgress() > 0;
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

    protected void doAutoSmeltTick() {
        if (!isEnabled()) return;
        if (!(storage instanceof BackpackWrapper bw)) return;

        BackpackItemStackHandler invHandler = bw.backpackHandler;

        // Auto-pull input from backpack
        ItemStack input = getInput();
        if (input == null || input.stackSize < input.getMaxStackSize()) {
            for (int i = 0; i < invHandler.getSlots(); i++) {
                ItemStack stack = invHandler.getStackInSlot(i);
                if (stack == null || stack.stackSize <= 0) continue;
                if (!checkFilter(stack)) continue;
                ItemStack result = getSmeltingResult(stack);
                if (result == null) continue;

                if (input == null) {
                    ItemStack pulled = stack.copy();
                    pulled.stackSize = 1;
                    setInput(pulled);
                    stack.stackSize--;
                    if (stack.stackSize <= 0) {
                        invHandler.setStackInSlot(i, null);
                    } else {
                        invHandler.setStackInSlot(i, stack);
                    }
                    input = getInput();
                    break;
                } else if (ItemHandlerHelper.canItemStacksStack(input, stack)) {
                    int space = input.getMaxStackSize() - input.stackSize;
                    int take = Math.min(space, stack.stackSize);
                    if (take > 0) {
                        input.stackSize += take;
                        setInput(input);
                        stack.stackSize -= take;
                        if (stack.stackSize <= 0) {
                            invHandler.setStackInSlot(i, null);
                        } else {
                            invHandler.setStackInSlot(i, stack);
                        }
                        break;
                    }
                }
            }
        }

        // Auto-pull fuel from backpack
        ItemStack fuel = getFuel();
        if (fuel == null || fuel.stackSize < fuel.getMaxStackSize()) {
            for (int i = 0; i < invHandler.getSlots(); i++) {
                ItemStack stack = invHandler.getStackInSlot(i);
                if (stack == null || stack.stackSize <= 0) continue;
                if (!checkFuelFilter(stack)) continue;
                int burnTime = TileEntityFurnace.getItemBurnTime(stack);
                if (burnTime <= 0) continue;

                if (fuel == null) {
                    ItemStack pulled = stack.copy();
                    pulled.stackSize = 1;
                    setFuel(pulled);
                    stack.stackSize--;
                    if (stack.stackSize <= 0) {
                        invHandler.setStackInSlot(i, null);
                    } else {
                        invHandler.setStackInSlot(i, stack);
                    }
                    fuel = getFuel();
                    break;
                } else if (ItemHandlerHelper.canItemStacksStack(fuel, stack)) {
                    int space = fuel.getMaxStackSize() - fuel.stackSize;
                    int take = Math.min(space, stack.stackSize);
                    if (take > 0) {
                        fuel.stackSize += take;
                        setFuel(fuel);
                        stack.stackSize -= take;
                        if (stack.stackSize <= 0) {
                            invHandler.setStackInSlot(i, null);
                        } else {
                            invHandler.setStackInSlot(i, stack);
                        }
                        break;
                    }
                }
            }
        }

        // Auto-push output to backpack
        ItemStack output = getOutput();
        if (output != null && output.stackSize > 0) {
            ItemStack remaining = tryInsertToBackpack(output, invHandler);
            setOutput(remaining);
        }

        // Do smelting tick
        doSmeltTick();
    }

    protected void doSmeltTick() {
        boolean dirty = false;
        int fuelProgress = getFuelProgress();
        int smeltProgress = getSmeltProgress();
        int fuelTotal = getFuelTotal();

        if (fuelProgress > 0) {
            fuelProgress--;
            setFuelProgress(fuelProgress);
            dirty = true;
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
                                setFuelProgress(fuelProgress);
                                setFuelTotal(fuelTotal);

                                fuel.stackSize--;
                                if (fuel.stackSize <= 0) {
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
                        if (smeltProgress >= getSmeltTime()) {
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
                        }
                        setSmeltProgress(smeltProgress);
                        dirty = true;
                    } else if (smeltProgress > 0) {
                        smeltProgress = Math.max(smeltProgress - 2, 0);
                        setSmeltProgress(smeltProgress);
                        dirty = true;
                    }
                }
            } else if (smeltProgress > 0) {
                smeltProgress = Math.max(smeltProgress - 2, 0);
                setSmeltProgress(smeltProgress);
                dirty = true;
            }
        } else if (smeltProgress > 0) {
            smeltProgress = Math.max(smeltProgress - 2, 0);
            setSmeltProgress(smeltProgress);
            dirty = true;
        }

        if (dirty) {
            markDirty();
        }
    }

    protected ItemStack tryInsertToBackpack(ItemStack output, BackpackItemStackHandler invHandler) {
        if (output == null) return null;

        ItemStack remaining = ItemHandlerHelper.copyStackWithSize(output, output.stackSize);

        for (int i = 0; i < invHandler.getSlots() && remaining != null; i++) {
            ItemStack existing = invHandler.getStackInSlot(i);
            if (existing == null) continue;
            if (!ItemHandlerHelper.canItemStacksStack(existing, remaining)) continue;

            int limit = invHandler.getSlotLimit(i);
            int space = limit - existing.stackSize;
            if (space <= 0) continue;

            int toInsert = Math.min(space, remaining.stackSize);
            existing.stackSize += toInsert;
            invHandler.setStackInSlot(i, existing);

            remaining.stackSize -= toInsert;
            if (remaining.stackSize <= 0) return null;
        }

        for (int i = 0; i < invHandler.getSlots() && remaining != null; i++) {
            ItemStack existing = invHandler.getStackInSlot(i);
            if (existing != null) continue;

            int limit = invHandler.getSlotLimit(i);
            int toInsert = Math.min(limit, remaining.stackSize);

            ItemStack placed = remaining.copy();
            placed.stackSize = toInsert;
            invHandler.setStackInSlot(i, placed);

            remaining.stackSize -= toInsert;
            if (remaining.stackSize <= 0) return null;
        }

        return remaining;
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        doAutoSmeltTick();
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        doAutoSmeltTick();
        return false;
    }
}
