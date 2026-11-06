package ruiseki.okstoragecore.upgrades.tank;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.fluid.capability.CapabilityFluidHandler;
import ruiseki.okcore.fluid.handler.IFluidHandlerItem;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.IStackableContentsUpgrade;
import ruiseki.okstoragecore.upgrades.IStorageUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class TankUpgradeWrapper extends UpgradeWrapperBase<TankUpgradeWrapper, ItemTankUpgrade>
    implements ITickableUpgrade, IStackableContentsUpgrade, IStorageUpgrade {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private static final String CONTENTS_TAG = "contents";
    // private Consumer<TankRenderInfo> updateTankRenderInfoCallback;
    private final ItemStackHandler inventory;
    private FluidStack contents;
    private long cooldownTime = 0;

    protected TankUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
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
                return isValidFluidItem(stack, false);
            }

            private boolean isValidOutputItem(ItemStack stack) {
                return isValidFluidItem(stack, true);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        NBTHelpers.getCompoundTag(upgrade, "inventory")
            .ifPresent(this.inventory::deserializeNBT);
        contents = getContents(upgrade);
    }

    public static FluidStack getContents(ItemStack upgrade) {
        return NBTHelpers.getCompoundTag(upgrade, CONTENTS_TAG)
            .map(FluidStack::loadFluidStackFromNBT)
            .orElse(FluidHelpers.EMPTY);
    }

    private boolean isValidFluidItem(ItemStack stack, boolean isOutput) {
        return CapabilityHelpers.getCapability(stack, CapabilityFluidHandler.FLUID_HANDLER_ITEM)
            .map(fluidHandler -> isValidFluidHandler(fluidHandler, isOutput))
            .orElse(false);
    }

    private boolean isValidFluidHandler(IFluidHandlerItem fluidHandler, boolean isOutput) {
        boolean tankEmpty = FluidHelpers.isEmpty(contents);
        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
            if ((isOutput && (FluidHelpers.isEmpty(fluidInTank) || (!tankEmpty && fluidInTank.isFluidEqual(contents))))
                || (!isOutput && !FluidHelpers.isEmpty(fluidInTank)
                    && (tankEmpty || contents.isFluidEqual(fluidInTank)))) {
                return true;
            }
        }
        return false;
    }

    public FluidStack getContents() {
        return contents;
    }

    public int getTankCapacity() {
        return upgradeItem.getTankCapacity(storageWrapper);
    }

    @Override
    public IItemHandlerModifiable getStorage() {
        return inventory;
    }

    private int getMaxInOut() {
        return Math.max(
            FluidHelpers.BUCKET_VOLUME,
            upgradeItem.getTankUpgradeConfig()
                .getMaxInputOutput() * storageWrapper.getNumberOfSlotRows()
                * upgradeItem.getAdjustedStackMultiplier(storageWrapper));
    }

    public int fill(FluidStack resource, boolean doFill, boolean ignoreInOutLimit) {
        int capacity = getTankCapacity();

        if (FluidHelpers.getAmount(contents) >= capacity
            || (!FluidHelpers.isEmpty(contents) && !resource.isFluidEqual(contents))) {
            return 0;
        }

        int toFill = Math.min(capacity - FluidHelpers.getAmount(contents), FluidHelpers.getAmount(resource));
        if (!ignoreInOutLimit) {
            toFill = Math.min(getMaxInOut(), toFill);
        }

        if (doFill) {
            if (FluidHelpers.isEmpty(contents)) {
                contents = new FluidStack(resource, toFill);
            } else {
                FluidHelpers.setAmount(contents, FluidHelpers.getAmount(contents) + toFill);
            }
            serializeContents();
        }

        return toFill;
    }

    private void serializeContents() {
        if (FluidHelpers.isEmpty(contents)) {
            NBTHelpers.removeTag(upgrade, CONTENTS_TAG);
        } else {
            NBTHelpers.setCompoundTag(upgrade, CONTENTS_TAG, contents.writeToNBT(new NBTTagCompound()));
        }
        save();
    }

    public FluidStack drain(int maxDrain, boolean doDrain, boolean ignoreInOutLimit) {
        if (FluidHelpers.isEmpty(contents)) {
            return FluidHelpers.EMPTY;
        }

        int toDrain = Math.min(maxDrain, FluidHelpers.getAmount(contents));
        if (!ignoreInOutLimit) {
            toDrain = Math.min(getMaxInOut(), toDrain);
        }

        FluidStack ret = new FluidStack(contents, toDrain);
        if (doDrain) {
            if (toDrain == FluidHelpers.getAmount(contents)) {
                contents = FluidHelpers.EMPTY;
            } else {
                FluidHelpers.setAmount(contents, FluidHelpers.getAmount(contents) - toDrain);
            }
            serializeContents();
        }

        return ret;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (world.getTotalWorldTime() < cooldownTime) {
            return;
        }

        AtomicBoolean didSomething = new AtomicBoolean(false);
        CapabilityHelpers.getCapability(inventory.getStackInSlot(INPUT_SLOT), CapabilityFluidHandler.FLUID_HANDLER_ITEM)
            .ifPresent(
                fluidHandler -> didSomething
                    .set(drainHandler(fluidHandler, stack -> inventory.setStackInSlot(INPUT_SLOT, stack))));
        CapabilityHelpers
            .getCapability(inventory.getStackInSlot(OUTPUT_SLOT), CapabilityFluidHandler.FLUID_HANDLER_ITEM)
            .ifPresent(
                fluidHandler -> didSomething
                    .set(fillHandler(fluidHandler, stack -> inventory.setStackInSlot(OUTPUT_SLOT, stack))));

        if (didSomething.get()) {
            cooldownTime = world.getTotalWorldTime() + upgradeItem.getTankUpgradeConfig()
                .getAutoFillDrainContainerCooldown();
        }
    }

    public boolean fillHandler(IFluidHandlerItem fluidHandler, Consumer<ItemStack> updateContainerStack) {
        if (!FluidHelpers.isEmpty(contents) && isValidFluidHandler(fluidHandler, true)) {
            int filled = fluidHandler.fill(
                new FluidStack(contents, Math.min(FluidHelpers.BUCKET_VOLUME, FluidHelpers.getAmount(contents))),
                false);
            if (filled <= 0) { // checking for less than as well because some mods have incorrect fill logic
                return false;
            }
            FluidStack drained = drain(filled, true, false);
            fluidHandler.fill(drained, true);
            updateContainerStack.accept(fluidHandler.getContainer());
            return true;
        }
        return false;
    }

    public boolean drainHandler(IFluidHandlerItem fluidHandler, Consumer<ItemStack> updateContainerStack) {
        if (isValidFluidHandler(fluidHandler, false)) {
            FluidStack extracted = FluidHelpers.isEmpty(contents)
                ? fluidHandler.drain(FluidHelpers.BUCKET_VOLUME, false)
                : fluidHandler.drain(
                    new FluidStack(
                        contents,
                        Math.min(FluidHelpers.BUCKET_VOLUME, getTankCapacity() - FluidHelpers.getAmount(contents))),
                    false);
            if (FluidHelpers.isEmpty(extracted)) {
                return false;
            }
            int filled = fill(extracted, true, false);
            FluidStack toExtract = filled == FluidHelpers.getAmount(extracted) ? extracted
                : new FluidStack(extracted, filled);
            fluidHandler.drain(toExtract, true);
            updateContainerStack.accept(fluidHandler.getContainer());
            return true;
        }
        return false;
    }

    public boolean transferCursorFluid(EntityPlayer player) {
        ItemStack cursorStack = player.inventory.getItemStack();
        return CapabilityHelpers.getCapability(cursorStack, CapabilityFluidHandler.FLUID_HANDLER_ITEM)
            .map(fluidHandler -> {
                boolean hasFluid = false;
                for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                    if (!FluidHelpers.isEmpty(fluidHandler.getFluidInTank(tank))) {
                        hasFluid = true;
                        break;
                    }
                }
                if (hasFluid) {
                    // Container -> Tank
                    return drainHandler(fluidHandler, player.inventory::setItemStack);
                }

                // Tank -> Container
                return fillHandler(fluidHandler, player.inventory::setItemStack);
            })
            .orElse(false);
    }

    @Override
    public int getMinimumMultiplierRequired() {
        return (int) Math.ceil((float) FluidHelpers.getAmount(contents) / upgradeItem.getBaseCapacity(storageWrapper));
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }
}
