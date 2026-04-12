package ruiseki.okbackpack.common.item.tank;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;
import ruiseki.okcore.helper.LangHelpers;

public class TankUpgradeWrapper extends UpgradeWrapperBase implements ITankUpgrade {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int INPUT_RESULT_SLOT = 2;
    public static final int OUTPUT_RESULT_SLOT = 3;

    public static final String CONTENTS_TAG = "TankContents";
    public static final String TANK_INV_TAG = "TankInv";

    public static final int BASE_CAPACITY_PER_SLOT = 1000;
    public static final int BASE_MAX_TRANSFER = 1000;

    protected final BaseItemStackHandler tankInventory;
    protected FluidStack contents;

    private int tickCooldown = 0;
    private static final int AUTO_COOLDOWN = 20;

    public TankUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);

        this.tankInventory = new BaseItemStackHandler(4) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(TANK_INV_TAG, this.serializeNBT());
                save();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == INPUT_SLOT) return TankUpgradeWrapper.this.isValidInputItem(stack);
                if (slot == OUTPUT_SLOT) return TankUpgradeWrapper.this.isValidOutputItem(stack);
                return true; // result slots accept internal outputs
            }
        };

        NBTTagCompound invTag = ItemNBTHelpers.getCompound(upgrade, TANK_INV_TAG, false);
        if (invTag != null) tankInventory.deserializeNBT(invTag);
        // Migrate old 2-slot saves: resize stacks list first, then set visualSize
        if (tankInventory.isSizeInconsistent(4)) {
            tankInventory.resize(4);
        }
        if (tankInventory.getVisualSize() < 4) {
            tankInventory.setVisualSize(4);
        }

        this.contents = loadContents(upgrade);
    }

    public static FluidStack loadContents(ItemStack upgrade) {
        NBTTagCompound contentsTag = ItemNBTHelpers.getCompound(upgrade, CONTENTS_TAG, false);
        if (contentsTag == null) return null;
        return FluidStack.loadFluidStackFromNBT(contentsTag);
    }

    @Override
    public void onAdded() {
        int size = storage.getStackHandler()
            .getVisualSize();
        size -= ItemTankUpgrade.SLOTS_NEEDED;
        storage.getStackHandler()
            .setVisualSize(size);
        storage.markDirty();
    }

    @Override
    public void onBeforeRemoved() {
        int size = storage.getStackHandler()
            .getVisualSize();
        size += ItemTankUpgrade.SLOTS_NEEDED;
        storage.getStackHandler()
            .setVisualSize(size);
        storage.markDirty();
    }

    @Override
    public FluidStack getContents() {
        if (contents == null) {
            contents = loadContents(upgrade);
        }
        return contents;
    }

    @Override
    public int getTankCapacity() {
        double stackMultiplier = storage.applyStackLimitModifiers();
        return (int) (ItemTankUpgrade.SLOTS_NEEDED * BASE_CAPACITY_PER_SLOT * stackMultiplier);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) return 0;

        int capacity = getTankCapacity();
        int currentAmount = contents != null ? contents.amount : 0;

        if (currentAmount >= capacity) return 0;
        if (contents != null && !contents.isFluidEqual(resource)) return 0;

        int toFill = Math.min(capacity - currentAmount, resource.amount);
        toFill = Math.min(getMaxInOut(), toFill);

        if (doFill) {
            if (contents == null) {
                contents = new FluidStack(resource, toFill);
            } else {
                contents.amount += toFill;
            }
            serializeContents();
        }

        return toFill;
    }

    public int fillIgnoreLimit(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) return 0;

        int capacity = getTankCapacity();
        int currentAmount = contents != null ? contents.amount : 0;

        if (currentAmount >= capacity) return 0;
        if (contents != null && !contents.isFluidEqual(resource)) return 0;

        int toFill = Math.min(capacity - currentAmount, resource.amount);

        if (doFill) {
            if (contents == null) {
                contents = new FluidStack(resource, toFill);
            } else {
                contents.amount += toFill;
            }
            serializeContents();
        }

        return toFill;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (contents == null || contents.amount <= 0) return null;

        int toDrain = Math.min(maxDrain, contents.amount);
        toDrain = Math.min(getMaxInOut(), toDrain);

        FluidStack ret = new FluidStack(contents, toDrain);
        if (doDrain) {
            contents.amount -= toDrain;
            if (contents.amount <= 0) {
                contents = null;
            }
            serializeContents();
        }

        return ret;
    }

    public FluidStack drainIgnoreLimit(int maxDrain, boolean doDrain) {
        if (contents == null || contents.amount <= 0) return null;

        int toDrain = Math.min(maxDrain, contents.amount);

        FluidStack ret = new FluidStack(contents, toDrain);
        if (doDrain) {
            contents.amount -= toDrain;
            if (contents.amount <= 0) {
                contents = null;
            }
            serializeContents();
        }

        return ret;
    }

    @Override
    public float getFillRatio() {
        int capacity = getTankCapacity();
        if (capacity <= 0) return 0f;
        int amount = contents != null ? contents.amount : 0;
        return (float) amount / capacity;
    }

    @Override
    public BaseItemStackHandler getStorage() {
        return tankInventory;
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.tank_settings";
    }

    private int getMaxInOut() {
        double stackMultiplier = storage.applyStackLimitModifiers();
        return (int) (BASE_MAX_TRANSFER * stackMultiplier);
    }

    private void serializeContents() {
        NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
        if (contents != null && contents.amount > 0) {
            NBTTagCompound contentsTag = new NBTTagCompound();
            contents.writeToNBT(contentsTag);
            tag.setTag(CONTENTS_TAG, contentsTag);
        } else {
            tag.removeTag(CONTENTS_TAG);
        }
        if (upgradeConsumer != null) {
            upgradeConsumer.accept(upgrade);
        }
        if (storage != null) {
            storage.markDirty();
        }
        save();
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

    private boolean doTick() {
        ensureResultSlotsVisible();
        if (tickCooldown > 0) {
            tickCooldown--;
            return false;
        }

        boolean didSomething = false;

        // Input slot: drain fluid from container into tank
        ItemStack inputStack = tankInventory.getStackInSlot(INPUT_SLOT);
        if (inputStack != null) {
            didSomething |= tryDrainFromItem(inputStack, INPUT_SLOT);
        }

        // Output slot: fill fluid from tank into container
        ItemStack outputStack = tankInventory.getStackInSlot(OUTPUT_SLOT);
        if (outputStack != null) {
            didSomething |= tryFillIntoItem(outputStack, OUTPUT_SLOT);
        }

        if (didSomething) {
            tickCooldown = AUTO_COOLDOWN;
            serializeContents();
        }
        return didSomething;
    }

    private void ensureResultSlotsVisible() {
        if (tankInventory.isSizeInconsistent(4)) {
            tankInventory.resize(4);
        }
        if (tankInventory.getVisualSize() < 4) {
            tankInventory.setVisualSize(4);
        }
    }

    private boolean canStacksMerge(ItemStack first, ItemStack second) {
        return first != null && second != null
            && first.isItemEqual(second)
            && ItemStack.areItemStackTagsEqual(first, second);
    }

    private boolean canMoveToResultSlot(int resultSlot, ItemStack resultStack) {
        ensureResultSlotsVisible();
        if (resultStack == null || resultStack.stackSize <= 0) {
            return true;
        }

        ItemStack existing = tankInventory.getStackInSlot(resultSlot);
        int slotLimit = Math.min(tankInventory.getSlotLimit(resultSlot), resultStack.getMaxStackSize());

        if (existing == null) {
            return resultStack.stackSize <= slotLimit;
        }

        if (!canStacksMerge(existing, resultStack)) {
            return false;
        }

        int maxMerged = Math.min(slotLimit, existing.getMaxStackSize());
        return existing.stackSize + resultStack.stackSize <= maxMerged;
    }

    private boolean moveToResultSlot(int resultSlot, ItemStack resultStack) {
        ensureResultSlotsVisible();
        if (!canMoveToResultSlot(resultSlot, resultStack)) {
            return false;
        }

        ItemStack toInsert = resultStack.copy();
        ItemStack remaining = tankInventory.insertItem(resultSlot, toInsert, false);
        return remaining == null || remaining.stackSize <= 0;
    }

    private boolean tryDrainFromItem(ItemStack stack, int slot) {
        // Try IFluidContainerItem first
        if (stack.getItem() instanceof IFluidContainerItem containerItem) {
            ItemStack single = stack.copy();
            single.stackSize = 1;

            FluidStack contained = containerItem.getFluid(single);
            if (contained == null || contained.amount <= 0) return false;
            if (contents != null && !contents.isFluidEqual(contained)) return false;

            FluidStack simDrain = containerItem.drain(single, getMaxInOut(), false);
            if (simDrain == null || simDrain.amount <= 0) return false;

            int filled = fillIgnoreLimit(simDrain, false);
            if (filled <= 0) return false;

            // Check if this drain would empty the container
            boolean wouldEmpty = filled >= contained.amount;
            if (wouldEmpty) {
                // Simulate: can the result slot accept the drained container?
                ItemStack simStack = single.copy();
                containerItem.drain(simStack, filled, true);
                if (!canMoveToResultSlot(INPUT_RESULT_SLOT, simStack)) {
                    return false; // Result slot full, don't drain
                }
            }

            FluidStack drained = containerItem.drain(single, filled, true);
            if (drained != null && drained.amount > 0) {
                fillIgnoreLimit(drained, true);

                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    tankInventory.setStackInSlot(slot, null);
                } else {
                    tankInventory.setStackInSlot(slot, stack);
                }

                // Check if container is now empty
                FluidStack remaining = containerItem.getFluid(single);
                if (remaining == null || remaining.amount <= 0) {
                    if (!moveToResultSlot(INPUT_RESULT_SLOT, single)) {
                        return false;
                    }
                }
                return true;
            }
        }

        // Try FluidContainerRegistry
        ItemStack single = stack.copy();
        single.stackSize = 1;

        FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(single);
        if (contained != null) {
            if (contents != null && !contents.isFluidEqual(contained)) return false;
            int filled = fillIgnoreLimit(contained, false);
            if (filled >= contained.amount) {
                ItemStack empty = FluidContainerRegistry.drainFluidContainer(single);
                // Pre-check: can the result slot accept the empty container?
                if (empty != null && !canMoveToResultSlot(INPUT_RESULT_SLOT, empty)) {
                    return false; // Result slot full, don't drain
                }

                if (empty != null && !moveToResultSlot(INPUT_RESULT_SLOT, empty)) {
                    return false;
                }

                fillIgnoreLimit(contained, true);

                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    tankInventory.setStackInSlot(slot, null);
                } else {
                    tankInventory.setStackInSlot(slot, stack);
                }

                return true;
            }
        }

        return false;
    }

    private boolean tryFillIntoItem(ItemStack stack, int slot) {
        if (contents == null || contents.amount <= 0) return false;

        ItemStack single = stack.copy();
        single.stackSize = 1;

        // Try IFluidContainerItem first
        if (single.getItem() instanceof IFluidContainerItem containerItem) {
            FluidStack toFill = new FluidStack(contents, Math.min(getMaxInOut(), contents.amount));
            int simFilled = containerItem.fill(single, toFill, false);
            if (simFilled <= 0) return false;

            FluidStack drained = drainIgnoreLimit(simFilled, false);
            if (drained == null || drained.amount <= 0) return false;

            // Check if filling would make the container full
            FluidStack currentFluid = containerItem.getFluid(single);
            int currentAmount = currentFluid != null ? currentFluid.amount : 0;
            int capacity = containerItem.getCapacity(single);
            boolean wouldFull = (currentAmount + simFilled >= capacity);

            if (wouldFull) {
                // Simulate: can the result slot accept the filled container?
                ItemStack simStack = single.copy();
                containerItem.fill(simStack, drained, true);
                if (!canMoveToResultSlot(OUTPUT_RESULT_SLOT, simStack)) {
                    return false; // Result slot full, don't fill
                }
            }

            int actualFilled = containerItem.fill(single, drained, true);
            if (actualFilled > 0) {
                drainIgnoreLimit(actualFilled, true);

                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    tankInventory.setStackInSlot(slot, null);
                } else {
                    tankInventory.setStackInSlot(slot, stack);
                }

                // Check if container is now full
                FluidStack afterFill = containerItem.getFluid(single);
                if (afterFill != null && afterFill.amount >= containerItem.getCapacity(single)) {
                    if (!moveToResultSlot(OUTPUT_RESULT_SLOT, single)) {
                        return false;
                    }
                }
                return true;
            }
        }

        // Try FluidContainerRegistry
        ItemStack filled = FluidContainerRegistry.fillFluidContainer(contents, single);
        if (filled != null) {
            int fillAmount = FluidContainerRegistry.getContainerCapacity(contents, single);
            if (fillAmount > 0 && contents.amount >= fillAmount) {
                // Pre-check: can the result slot accept the filled container?
                if (!canMoveToResultSlot(OUTPUT_RESULT_SLOT, filled)) {
                    return false; // Result slot full, don't fill
                }

                if (!moveToResultSlot(OUTPUT_RESULT_SLOT, filled)) {
                    return false;
                }

                drainIgnoreLimit(fillAmount, true);

                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    tankInventory.setStackInSlot(slot, null);
                } else {
                    tankInventory.setStackInSlot(slot, stack);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void interactWithCursorStack(EntityPlayer player) {
        ItemStack cursorStack = player.inventory.getItemStack();
        if (cursorStack == null) return;
        if (cursorStack.stackSize > 1) return;

        // Try IFluidContainerItem
        if (cursorStack.getItem() instanceof IFluidContainerItem containerItem) {
            if (contents == null || contents.amount <= 0) {
                // Tank empty -> drain from cursor
                drainFromCursor(containerItem, cursorStack, player);
            } else {
                // Tank has fluid -> try fill cursor first, then drain from cursor
                if (!fillCursor(containerItem, cursorStack, player)) {
                    drainFromCursor(containerItem, cursorStack, player);
                }
            }
            return;
        }

        // Try FluidContainerRegistry
        FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(cursorStack);
        if (contained != null) {
            // Cursor has a registered filled container -> drain into tank
            if (contents != null && !contents.isFluidEqual(contained)) return;
            int filled = fillIgnoreLimit(contained, false);
            if (filled >= contained.amount) {
                fillIgnoreLimit(contained, true);
                ItemStack empty = FluidContainerRegistry.drainFluidContainer(cursorStack);
                player.inventory.setItemStack(empty);
            }
        } else if (contents != null && contents.amount > 0) {
            // Cursor is an empty container -> try filling it
            ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(contents, cursorStack);
            if (filledContainer != null) {
                int fillAmount = FluidContainerRegistry.getContainerCapacity(contents, cursorStack);
                if (fillAmount > 0 && contents.amount >= fillAmount) {
                    drainIgnoreLimit(fillAmount, true);
                    player.inventory.setItemStack(filledContainer);
                }
            }
        }
    }

    private void drainFromCursor(IFluidContainerItem containerItem, ItemStack cursorStack, EntityPlayer player) {
        FluidStack contained = containerItem.getFluid(cursorStack);
        if (contained == null || contained.amount <= 0) return;
        if (contents != null && !contents.isFluidEqual(contained)) return;

        FluidStack drained = containerItem.drain(cursorStack, getMaxInOut(), false);
        if (drained == null || drained.amount <= 0) return;

        int filled = fillIgnoreLimit(drained, false);
        if (filled <= 0) return;

        FluidStack actualDrained = containerItem.drain(cursorStack, filled, true);
        if (actualDrained != null && actualDrained.amount > 0) {
            fillIgnoreLimit(actualDrained, true);
            player.inventory.setItemStack(cursorStack);
        }
    }

    private boolean fillCursor(IFluidContainerItem containerItem, ItemStack cursorStack, EntityPlayer player) {
        if (contents == null || contents.amount <= 0) return false;

        FluidStack toFill = new FluidStack(contents, Math.min(getMaxInOut(), contents.amount));
        int simFilled = containerItem.fill(cursorStack, toFill, false);
        if (simFilled <= 0) return false;

        FluidStack drained = drainIgnoreLimit(simFilled, false);
        if (drained == null || drained.amount <= 0) return false;

        int actualFilled = containerItem.fill(cursorStack, drained, true);
        if (actualFilled > 0) {
            drainIgnoreLimit(actualFilled, true);
            player.inventory.setItemStack(cursorStack);
            return true;
        }
        return false;
    }

    public boolean isFluidContainer(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() instanceof IFluidContainerItem) return true;
        return FluidContainerRegistry.isContainer(stack);
    }

    public boolean isValidInputItem(ItemStack stack) {
        return isFluidContainer(stack);
    }

    public boolean isValidOutputItem(ItemStack stack) {
        return isFluidContainer(stack);
    }

    @Override
    public List<String> getTooltipLines() {
        FluidStack fluid = getContents();
        if (fluid != null && fluid.amount > 0) {
            return Collections.singletonList(
                "\u00a7e" + LangHelpers
                    .localize("tooltip.backpack.contents.fluid", fluid.amount, "\u00a79" + fluid.getLocalizedName()));
        }
        return Collections.singletonList("\u00a79" + LangHelpers.localize("tooltip.backpack.contents.fluid_empty"));
    }
}
