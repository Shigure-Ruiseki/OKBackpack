package ruiseki.okstoragecore.upgrades;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.helper.NBTHelpers;

public class FluidFilterLogic extends FluidFilterLogicBase {

    private final int filterSlotCount;
    private final Predicate<FluidStack> isFluidValid;

    private ObservableFilterFluidHandler filterHandler = null;
    private boolean emptyAllowListMatchesEverything = false;

    public FluidFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount) {
        this(upgrade, saveHandler, filterSlotCount, fluid -> true, "");
    }

    public FluidFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount,
        String parentTagKey) {
        this(upgrade, saveHandler, filterSlotCount, fluid -> true, parentTagKey);
    }

    public FluidFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount,
        Predicate<FluidStack> isFluidValid) {
        this(upgrade, saveHandler, filterSlotCount, isFluidValid, "");
    }

    public FluidFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount,
        Predicate<FluidStack> isFluidValid, String parentTagKey) {

        super(upgrade, saveHandler, parentTagKey);

        this.filterSlotCount = filterSlotCount;
        this.isFluidValid = isFluidValid;
    }

    public ObservableFilterFluidHandler getFilterHandler() {
        if (this.filterHandler == null) {
            this.filterHandler = new ObservableFilterFluidHandler();

            NBTHelpers.getCompoundTag(this.upgrade, this.parentTagKey, "filters")
                .ifPresent(this.filterHandler::deserializeNBT);
        }

        return this.filterHandler;
    }

    public boolean matchesFilter(FluidStack fluid) {
        if (FluidHelpers.isEmpty(fluid)) {
            return false;
        }

        if (isAllowList()) {
            return (getFilterHandler().hasOnlyEmptyFilters() && emptyAllowListMatchesEverything)
                || !getFilterHandler().doesNotContain(fluid);
        }

        return getFilterHandler().hasOnlyEmptyFilters() || getFilterHandler().doesNotContain(fluid);
    }

    public class ObservableFilterFluidHandler extends FluidTankHandler {

        private IntConsumer onTankChange = slot -> {};

        public ObservableFilterFluidHandler() {
            super(filterSlotCount);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            NBTHelpers.setCompoundNBT(upgrade, parentTagKey, "filters", serializeNBT());
            save();
            this.onTankChange.accept(slot);
        }

        public void setOnTankChange(IntConsumer onTankChange) {
            this.onTankChange = onTankChange;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return FluidHelpers.isEmpty(stack) || (doesNotContain(stack) && isFluidValid.test(stack));
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
        }

        private boolean doesNotContain(FluidStack fluid) {
            return doesNotContain(existing -> existing.isFluidEqual(fluid));
        }
    }
}
