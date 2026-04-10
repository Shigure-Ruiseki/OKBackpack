package ruiseki.okbackpack.common.item.battery;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

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
import ruiseki.okbackpack.client.gui.widget.upgrade.BatterySlotWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.BatteryUpgradeWidget;
import ruiseki.okbackpack.client.gui.widget.upgrade.ExpandedTabWidget;
import ruiseki.okbackpack.common.block.BackpackPanel;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okbackpack.common.item.stack.ItemStackUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemBatteryUpgrade extends ItemUpgrade<BatteryUpgradeWrapper> {

    public static final int SLOTS_NEEDED = 20;

    private BatterySlotWidget lastSlotWidget;

    public ItemBatteryUpgrade() {
        super("battery_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "battery_upgrade");
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
        list.add(LangHelpers.localize("tooltip.backpack.battery_upgrade"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        // Check single-instance constraint
        int[] conflicts = IUpgradeItem.findConflictSlots(wrapper, targetSlot, ItemBatteryUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult.failOnlySingleAllowed(
                conflicts,
                LangHelpers.localize("item.battery_upgrade.name"),
                wrapper.getDisplayName());
        }

        // getVisualSize() already accounts for slots reserved by existing storage upgrades (tank, etc.)
        int visualEnd = wrapper.getStackHandler()
            .getVisualSize();
        int[] filledInTail = wrapper.getStackHandler()
            .getFilledSlotsInRange(visualEnd - SLOTS_NEEDED, visualEnd);
        if (filledInTail.length > 0) {
            return UpgradeSlotChangeResult.failWithInventoryConflicts(
                "gui.backpack.error.add.needs_occupied_inventory_slots",
                filledInTail,
                SLOTS_NEEDED,
                LangHelpers.localize("item.battery_upgrade.name"));
        }

        // Check if stored energy exceeds capacity with current stack multiplier
        int storedEnergy = BatteryUpgradeWrapper.getEnergyStoredStatic(upgradeStack);
        if (storedEnergy > 0) {
            double currentMultiplier = wrapper.applyStackLimitModifiers();
            int slots = wrapper.getStackHandler()
                .getSlots();
            int capacity = (int) (BatteryUpgradeWrapper.BASE_ENERGY_PER_SLOT * slots * currentMultiplier);
            if (storedEnergy > capacity) {
                double requiredMultiplier = (double) storedEnergy
                    / (BatteryUpgradeWrapper.BASE_ENERGY_PER_SLOT * slots);
                return UpgradeSlotChangeResult.failUpgradeHigh(
                    new int[0],
                    LangHelpers.localize("item.battery_upgrade.name"),
                    ItemStackUpgrade.formatMultiplier(requiredMultiplier));
            }
        }

        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public BatteryUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        return new BatteryUpgradeWrapper(stack, storage, upgradeConsumer);
    }

    @Override
    public void updateWidgetDelegates(BatteryUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedStackHandlerSH batteryHandler = group.get("battery_inv_handler");
        if (batteryHandler == null) return;
        batteryHandler.setDelegatedStackHandler(wrapper::getStorage);
        batteryHandler.syncToServer(DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_STORAGE));

        DelegatedIntSH energyStoredHandler = group.get("battery_energy_stored");
        if (energyStoredHandler == null) return;
        energyStoredHandler.setDelegatedSupplier(wrapper::getEnergyStored);
        energyStoredHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_BATTERY_ENERGY));

        DelegatedIntSH maxEnergyHandler = group.get("battery_max_energy");
        if (maxEnergyHandler == null) return;
        maxEnergyHandler.setDelegatedSupplier(wrapper::getMaxEnergyStored);
        maxEnergyHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_BATTERY_MAX_ENERGY));

        DelegatedFloatSH chargeRatioHandler = group.get("battery_charge_ratio");
        if (chargeRatioHandler == null) return;
        chargeRatioHandler.setDelegatedSupplier(wrapper::getChargeRatio);
        chargeRatioHandler.syncToServer(DelegatedFloatSH.getId(DelegatedValueSHRegisters.UPDATE_BATTERY_CHARGE_RATIO));
    }

    @Override
    public void updateSlotWidgetDelegates(BatteryUpgradeWrapper wrapper, UpgradeSlotUpdateGroup group) {
        DelegatedFloatSH chargeRatioHandler = group.get("battery_charge_ratio");
        if (chargeRatioHandler == null) return;
        chargeRatioHandler.setDelegatedSupplier(wrapper::getChargeRatio);
        chargeRatioHandler.syncToServer(DelegatedFloatSH.getId(DelegatedValueSHRegisters.UPDATE_BATTERY_CHARGE_RATIO));

        DelegatedIntSH energyStoredHandler = group.get("battery_energy_stored");
        if (energyStoredHandler == null) return;
        energyStoredHandler.setDelegatedSupplier(wrapper::getEnergyStored);
        energyStoredHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_BATTERY_ENERGY));

        DelegatedIntSH maxEnergyHandler = group.get("battery_max_energy");
        if (maxEnergyHandler == null) return;
        maxEnergyHandler.setDelegatedSupplier(wrapper::getMaxEnergyStored);
        maxEnergyHandler.syncToServer(DelegatedIntSH.getId(DelegatedValueSHRegisters.UPDATE_BATTERY_MAX_ENERGY));

        if (lastSlotWidget != null) {
            lastSlotWidget.setEnergySuppliers(energyStoredHandler::getIntValue, maxEnergyHandler::getIntValue);
            lastSlotWidget = null;
        }
    }

    @Override
    public Widget<?> getSlotWidget(int slotIndex, BatteryUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        BackpackPanel backpackPanel = panel instanceof BackpackPanel ? (BackpackPanel) panel : null;
        lastSlotWidget = new BatterySlotWidget(slotIndex, wrapper, backpackPanel);
        return lastSlotWidget;
    }

    @Override
    public ExpandedTabWidget getExpandedTabWidget(int slotIndex, BatteryUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        return new BatteryUpgradeWidget(slotIndex, wrapper, stack, panel, titleKey);
    }
}
