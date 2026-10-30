package ruiseki.okstoragecore.client.gui.sync.value;

import net.minecraft.util.ResourceLocation;

import cofh.api.energy.IEnergyStorage;
import ruiseki.okstoragecore.Reference;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegistry;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class DelegatedValueSHRegisters {

    public static final StorageSHRegistry.Entry<DelegatedValueSH> REGISTRY = StorageSHRegistry
        .get(DelegatedValueSH.class);

    public static final ResourceLocation BATTERY_ENERGY = new ResourceLocation(
        Reference.MOD_ID,
        "storage_batter_energy");
    public static final ResourceLocation BATTERY_MAX_ENERGY = new ResourceLocation(
        Reference.MOD_ID,
        "storage_batter_max_energy");

    public static void load() {
        REGISTRY.registerServer(BATTERY_ENERGY, (value, buf) -> {
            IUpgradeWrapper wrapper = value.getUpgradeWrapper();
            if (!(value instanceof DelegatedIntSH handler)) return;
            if (!(wrapper instanceof IEnergyStorage upgrade)) return;
            handler.setDelegatedSupplier(upgrade::getEnergyStored);
        });
        REGISTRY.registerServer(BATTERY_MAX_ENERGY, (value, buf) -> {
            IUpgradeWrapper wrapper = value.getUpgradeWrapper();
            if (!(value instanceof DelegatedIntSH handler)) return;
            if (!(wrapper instanceof IEnergyStorage upgrade)) return;
            handler.setDelegatedSupplier(upgrade::getMaxEnergyStored);
        });
    }
}
