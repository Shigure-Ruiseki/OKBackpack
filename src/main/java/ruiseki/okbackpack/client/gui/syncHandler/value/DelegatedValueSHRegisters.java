package ruiseki.okbackpack.client.gui.syncHandler.value;

import ruiseki.okbackpack.api.upgrade.DelegatedValueSHRegistry;
import ruiseki.okbackpack.api.wrapper.IBatteryUpgrade;
import ruiseki.okbackpack.api.wrapper.IProgressable;
import ruiseki.okbackpack.api.wrapper.ISmeltingUpgrade;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okcore.init.IInitListener;

public class DelegatedValueSHRegisters implements IInitListener {

    public static final String UPDATE_PROGRESS = "update_progress";
    public static final String UPDATE_FUEL = "update_fuel";
    public static final String UPDATE_BATTERY_ENERGY = "update_battery_energy";
    public static final String UPDATE_BATTERY_MAX_ENERGY = "update_battery_max_energy";
    public static final String UPDATE_BATTERY_CHARGE_RATIO = "update_battery_charge_ratio";
    public static final String UPDATE_TANK_FLUID_AMOUNT = "update_tank_fluid_amount";
    public static final String UPDATE_TANK_CAPACITY = "update_tank_capacity";
    public static final String UPDATE_TANK_FILL_RATIO = "update_tank_fill_ratio";
    public static final String UPDATE_TANK_FLUID_ID = "update_tank_fluid_id";

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {

            DelegatedValueSHRegistry.registerServer(UPDATE_PROGRESS, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedFloatSH handler)) return;
                if (!(wrapper instanceof IProgressable upgrade)) return;
                handler.setDelegatedSupplier(upgrade::getProgress);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_FUEL, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedFloatSH handler)) return;
                if (!(wrapper instanceof ISmeltingUpgrade upgrade)) return;
                handler.setDelegatedSupplier(upgrade::getBurnProgress);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_BATTERY_ENERGY, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedIntSH handler)) return;
                if (!(wrapper instanceof IBatteryUpgrade upgrade)) return;
                handler.setDelegatedSupplier(upgrade::getEnergyStored);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_BATTERY_MAX_ENERGY, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedIntSH handler)) return;
                if (!(wrapper instanceof IBatteryUpgrade upgrade)) return;
                handler.setDelegatedSupplier(upgrade::getMaxEnergyStored);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_BATTERY_CHARGE_RATIO, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedFloatSH handler)) return;
                if (!(wrapper instanceof IBatteryUpgrade upgrade)) return;
                handler.setDelegatedSupplier(upgrade::getChargeRatio);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_TANK_FLUID_AMOUNT, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedIntSH handler)) return;
                if (!(wrapper instanceof ITankUpgrade upgrade)) return;
                handler.setDelegatedSupplier(() -> upgrade.getContents() != null ? upgrade.getContents().amount : 0);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_TANK_CAPACITY, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedIntSH handler)) return;
                if (!(wrapper instanceof ITankUpgrade upgrade)) return;
                handler.setDelegatedSupplier(upgrade::getTankCapacity);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_TANK_FILL_RATIO, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedFloatSH handler)) return;
                if (!(wrapper instanceof ITankUpgrade upgrade)) return;
                handler.setDelegatedSupplier(upgrade::getFillRatio);
            });

            DelegatedValueSHRegistry.registerServer(UPDATE_TANK_FLUID_ID, (value, buf) -> {
                IUpgradeWrapper wrapper = value.getWrapper();
                if (!(value instanceof DelegatedIntSH handler)) return;
                if (!(wrapper instanceof ITankUpgrade upgrade)) return;
                handler.setDelegatedSupplier(
                    () -> upgrade.getContents() != null ? upgrade.getContents()
                        .getFluidID() : -1);
            });
        }
    }
}
