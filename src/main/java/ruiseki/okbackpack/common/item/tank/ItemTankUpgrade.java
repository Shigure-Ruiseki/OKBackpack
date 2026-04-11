package ruiseki.okbackpack.common.item.tank;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.widget.Widget;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedFloatSH;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedIntSH;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedValueSHRegisters;
import ruiseki.okbackpack.client.gui.widget.updateGroup.UpgradeSlotUpdateGroup;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.TankSlotWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.TankUpgradeWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.common.item.stack.ItemStackUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemTankUpgrade extends ItemUpgrade<TankUpgradeWrapper> {

    public static final int SLOTS_NEEDED = 20;
    public static final int MAX_TANK_UPGRADES = 2;

    private TankSlotWidget lastSlotWidget;

    public ItemTankUpgrade() {
        super("tank_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "tank_upgrade");
    }

    @Override
    public boolean hasTab() {
        return true;
    }

    @Override
    public boolean hasSlotWidget() {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.tank_upgrade"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        // Check max 2 tank upgrades
        int[] conflicts = IUpgradeItem.findConflictSlots(wrapper, targetSlot, ItemTankUpgrade.class);
        if (conflicts.length >= MAX_TANK_UPGRADES) {
            return UpgradeSlotChangeResult.failOnlyXAllowed(
                conflicts,
                MAX_TANK_UPGRADES,
                LangHelpers.localize("item.tank_upgrade.name"),
                wrapper.getDisplayName());
        }

        // getVisualSize() already accounts for slots reserved by existing storage upgrades (battery, other tanks)
        int visualEnd = wrapper.getStackHandler()
            .getVisualSize();
        int startSlot = visualEnd - SLOTS_NEEDED;
        if (startSlot < 0) {
            return UpgradeSlotChangeResult
                .failUpgradeHigh(conflicts, LangHelpers.localize("item.tank_upgrade.name"), "1");
        }

        int[] filledInTail = wrapper.getStackHandler()
            .getFilledSlotsInRange(startSlot, visualEnd);
        if (filledInTail.length > 0) {
            return UpgradeSlotChangeResult.failWithInventoryConflicts(
                "gui.backpack.error.add.needs_occupied_inventory_slots",
                filledInTail,
                SLOTS_NEEDED,
                LangHelpers.localize("item.tank_upgrade.name"));
        }

        // Check if stored fluid exceeds capacity with current stack multiplier
        FluidStack storedContents = TankUpgradeWrapper.loadContents(upgradeStack);
        if (storedContents != null && storedContents.amount > 0) {
            double currentMultiplier = wrapper.applyStackLimitModifiers();
            int capacity = (int) (SLOTS_NEEDED * TankUpgradeWrapper.BASE_CAPACITY_PER_SLOT * currentMultiplier);
            if (storedContents.amount > capacity) {
                double requiredMultiplier = (double) storedContents.amount
                    / (SLOTS_NEEDED * TankUpgradeWrapper.BASE_CAPACITY_PER_SLOT);
                return UpgradeSlotChangeResult.failUpgradeHigh(
                    new int[0],
                    LangHelpers.localize("item.tank_upgrade.name"),
                    ItemStackUpgrade.formatMultiplier(requiredMultiplier));
            }
        }

        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public TankUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new TankUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public void updateWidgetDelegates(TankUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH tankHandler = group.get("tank_inv_handler");
        if (tankHandler == null) return;
        tankHandler.setDelegatedStackHandler(wrapper::getStorage);
        tankHandler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_STORAGE));

        DelegatedIntSH fluidAmountHandler = group.get("tank_fluid_amount");
        if (fluidAmountHandler != null) {
            fluidAmountHandler
                .setDelegatedSupplier(() -> wrapper.getContents() != null ? wrapper.getContents().amount : 0);
            fluidAmountHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_FLUID_AMOUNT));
        }

        DelegatedIntSH tankCapacityHandler = group.get("tank_capacity");
        if (tankCapacityHandler == null) return;
        tankCapacityHandler.setDelegatedSupplier(wrapper::getTankCapacity);
        tankCapacityHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_CAPACITY));

        DelegatedFloatSH fillRatioHandler = group.get("tank_fill_ratio");
        if (fillRatioHandler == null) return;
        fillRatioHandler.setDelegatedSupplier(wrapper::getFillRatio);
        fillRatioHandler.syncToServer(DelegatedFloatSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_FILL_RATIO));

        DelegatedIntSH fluidIdHandler = group.get("tank_fluid_id");
        if (fluidIdHandler != null) {
            fluidIdHandler.setDelegatedSupplier(() -> {
                FluidStack stack = wrapper.getContents();
                return (stack != null && stack.getFluid() != null) ? stack.getFluidID() : -1;
            });
            fluidIdHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_FLUID_ID));
        }
    }

    @Override
    public void updateSlotWidgetDelegates(TankUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedFloatSH fillRatioHandler = group.get("tank_fill_ratio");
        if (fillRatioHandler == null) return;
        fillRatioHandler.setDelegatedSupplier(wrapper::getFillRatio);
        fillRatioHandler.syncToServer(DelegatedFloatSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_FILL_RATIO));

        DelegatedIntSH fluidAmountHandler = group.get("tank_fluid_amount");
        if (fluidAmountHandler == null) return;
        fluidAmountHandler.setDelegatedSupplier(() -> wrapper.getContents() != null ? wrapper.getContents().amount : 0);
        fluidAmountHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_FLUID_AMOUNT));

        DelegatedIntSH tankCapacityHandler = group.get("tank_capacity");
        if (tankCapacityHandler == null) return;
        tankCapacityHandler.setDelegatedSupplier(wrapper::getTankCapacity);
        tankCapacityHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_CAPACITY));

        DelegatedIntSH fluidIdHandler = group.get("tank_fluid_id");
        if (fluidIdHandler == null) return;
        fluidIdHandler.setDelegatedSupplier(
            () -> wrapper.getContents() != null ? wrapper.getContents()
                .getFluidID() : -1);
        fluidIdHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_TANK_FLUID_ID));

        if (lastSlotWidget != null) {
            lastSlotWidget.setTankSuppliers(
                fluidAmountHandler::getIntValue,
                tankCapacityHandler::getIntValue,
                fluidIdHandler::getIntValue);
            lastSlotWidget = null;
        }
    }

    @Override
    public Widget<?> getSlotWidget(int slotIndex, TankUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        int tankCount = IUpgradeItem.countUpgrades(storage(panel), -1, ItemTankUpgrade.class);
        BackpackPanel backpackPanel = panel instanceof BackpackPanel ? (BackpackPanel) panel : null;
        lastSlotWidget = new TankSlotWidget(slotIndex, wrapper, Math.max(1, tankCount), backpackPanel);
        return lastSlotWidget;
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, TankUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new TankUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }

    private static IStorageWrapper storage(IStoragePanel<?> panel) {
        return panel.getWrapper();
    }
}
