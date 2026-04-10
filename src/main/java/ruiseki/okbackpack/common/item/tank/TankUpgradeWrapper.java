package ruiseki.okbackpack.common.item.tank;

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
            public int getSlotLimit(int slot) {
                if (slot == INPUT_RESULT_SLOT || slot == OUTPUT_RESULT_SLOT) return 64;
                return 1;
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
        }
        return didSomething;
    }

    private boolean hasInsertRemainder(ItemStack stack) {
        return stack != null && stack.stackSize > 0;
    }

    private boolean tryDrainFromItem(ItemStack stack, int slot) {
        // Try IFluidContainerItem first
        if (stack.getItem() instanceof IFluidContainerItem containerItem) {
            FluidStack contained = containerItem.getFluid(stack);
            if (contained == null || contained.amount <= 0) return false;
            if (contents != null && !contents.isFluidEqual(contained)) return false;

            FluidStack simDrain = containerItem.drain(stack, getMaxInOut(), false);
            if (simDrain == null || simDrain.amount <= 0) return false;

            int filled = fillIgnoreLimit(simDrain, false);
            if (filled <= 0) return false;

            // Check if this drain would empty the container
            boolean wouldEmpty = filled >= contained.amount;
            if (wouldEmpty) {
                // Simulate: can the result slot accept the drained container?
                ItemStack simStack = stack.copy();
                ((IFluidContainerItem) simStack.getItem()).drain(simStack, filled, true);
                if (hasInsertRemainder(tankInventory.insertItem(INPUT_RESULT_SLOT, simStack, true))) {
                    return false; // Result slot full, don't drain
                }
            }

            FluidStack drained = containerItem.drain(stack, filled, true);
            if (drained != null && drained.amount > 0) {
                fillIgnoreLimit(drained, true);

                // Check if container is now empty
                FluidStack remaining = containerItem.getFluid(stack);
                if (remaining == null || remaining.amount <= 0) {
                    tankInventory.setStackInSlot(slot, null);
                    tankInventory.insertItem(INPUT_RESULT_SLOT, stack, false);
                } else {
                    tankInventory.setStackInSlot(slot, stack);
                }
                return true;
            }
        }

        // Try FluidContainerRegistry
        FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(stack);
        if (contained != null) {
            if (contents != null && !contents.isFluidEqual(contained)) return false;
            int filled = fillIgnoreLimit(contained, false);
            if (filled >= contained.amount) {
                ItemStack empty = FluidContainerRegistry.drainFluidContainer(stack);
                // Pre-check: can the result slot accept the empty container?
                if (empty != null && hasInsertRemainder(tankInventory.insertItem(INPUT_RESULT_SLOT, empty, true))) {
                    return false; // Result slot full, don't drain
                }

                fillIgnoreLimit(contained, true);
                tankInventory.setStackInSlot(slot, null);
                if (empty != null) {
                    tankInventory.insertItem(INPUT_RESULT_SLOT, empty, false);
                }
                return true;
            }
        }

        return false;
    }

    private boolean tryFillIntoItem(ItemStack stack, int slot) {
        if (contents == null || contents.amount <= 0) return false;

        // Try IFluidContainerItem first
        if (stack.getItem() instanceof IFluidContainerItem containerItem) {
            FluidStack toFill = new FluidStack(contents, Math.min(getMaxInOut(), contents.amount));
            int simFilled = containerItem.fill(stack, toFill, false);
            if (simFilled <= 0) return false;

            FluidStack drained = drainIgnoreLimit(simFilled, false);
            if (drained == null || drained.amount <= 0) return false;

            // Check if filling would make the container full
            FluidStack currentFluid = containerItem.getFluid(stack);
            int currentAmount = currentFluid != null ? currentFluid.amount : 0;
            int capacity = containerItem.getCapacity(stack);
            boolean wouldFull = (currentAmount + simFilled >= capacity);

            if (wouldFull) {
                // Simulate: can the result slot accept the filled container?
                ItemStack simStack = stack.copy();
                ((IFluidContainerItem) simStack.getItem()).fill(simStack, drained, true);
                if (hasInsertRemainder(tankInventory.insertItem(OUTPUT_RESULT_SLOT, simStack, true))) {
                    return false; // Result slot full, don't fill
                }
            }

            int actualFilled = containerItem.fill(stack, drained, true);
            if (actualFilled > 0) {
                drainIgnoreLimit(actualFilled, true);

                // Check if container is now full
                FluidStack afterFill = containerItem.getFluid(stack);
                if (afterFill != null && afterFill.amount >= containerItem.getCapacity(stack)) {
                    tankInventory.setStackInSlot(slot, null);
                    tankInventory.insertItem(OUTPUT_RESULT_SLOT, stack, false);
                } else {
                    tankInventory.setStackInSlot(slot, stack);
                }
                return true;
            }
        }

        // Try FluidContainerRegistry
        ItemStack filled = FluidContainerRegistry.fillFluidContainer(contents, stack);
        if (filled != null) {
            int fillAmount = FluidContainerRegistry.getContainerCapacity(contents, stack);
            if (fillAmount > 0 && contents.amount >= fillAmount) {
                // Pre-check: can the result slot accept the filled container?
                if (hasInsertRemainder(tankInventory.insertItem(OUTPUT_RESULT_SLOT, filled, true))) {
                    return false; // Result slot full, don't fill
                }

                drainIgnoreLimit(fillAmount, true);
                tankInventory.setStackInSlot(slot, null);
                tankInventory.insertItem(OUTPUT_RESULT_SLOT, filled, false);
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
            FluidStack contained = containerItem.getFluid(cursorStack);

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

    public boolean isValidInputItem(ItemStack stack) {
        if (stack == null) return false;

        if (stack.getItem() instanceof IFluidContainerItem containerItem) {
            FluidStack contained = containerItem.getFluid(stack);
            if (contained != null && contained.amount > 0) {
                return contents == null || contents.isFluidEqual(contained);
            }
        }

        FluidStack registered = FluidContainerRegistry.getFluidForFilledItem(stack);
        if (registered != null) {
            return contents == null || contents.isFluidEqual(registered);
        }

        return false;
    }

    public boolean isValidOutputItem(ItemStack stack) {
        if (stack == null) return false;
        if (contents == null || contents.amount <= 0) return false;

        if (stack.getItem() instanceof IFluidContainerItem containerItem) {
            FluidStack contained = containerItem.getFluid(stack);
            if (contained == null || contained.amount == 0) return true;
            if (contained.isFluidEqual(contents) && contained.amount < containerItem.getCapacity(stack)) return true;
        }

        return FluidContainerRegistry.fillFluidContainer(contents, stack) != null;
    }
}
