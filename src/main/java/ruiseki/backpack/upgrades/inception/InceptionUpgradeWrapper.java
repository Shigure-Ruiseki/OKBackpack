package ruiseki.backpack.upgrades.inception;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import cofh.api.energy.IEnergyStorage;
import ruiseki.backpack.api.IEnergyStorageUpgradeWrapper;
import ruiseki.backpack.api.IFluidHandlerWrapperUpgrade;
import ruiseki.backpack.api.IInventoryWrapperUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okstoragecore.api.IStorageFluidHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.ITrackedContentsItemHandler;
import ruiseki.okstoragecore.upgrades.IUpgradeAccessModifier;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapperAccessor;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class InceptionUpgradeWrapper extends UpgradeWrapperBase<InceptionUpgradeWrapper, ItemInceptionUpgrade>
    implements IInventoryWrapperUpgrade, IUpgradeAccessModifier, IFluidHandlerWrapperUpgrade,
    IEnergyStorageUpgradeWrapper {

    private SubBackpacksHandler subBackpacksHandler = null;

    public InceptionUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(backpackWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean hideSettingsTab() {
        return !GeneralConfig.upgradesUseInventoriesOfBackpacksInBackpack;
    }

    public InventoryOrder getInventoryOrder() {
        return NBTHelpers.getEnumConstant(upgrade, "inventoryOrder", InventoryOrder::fromName)
            .orElse(InventoryOrder.MAIN_FIRST);
    }

    public void setInventoryOrder(InventoryOrder inventoryOrder) {
        NBTHelpers.setEnumConstant(upgrade, "inventoryOrder", inventoryOrder);
        save();
        storageWrapper.refreshInventoryForUpgradeProcessing();
    }

    @Override
    public ITrackedContentsItemHandler wrapInventory(ITrackedContentsItemHandler inventory) {
        if (GeneralConfig.upgradesUseInventoriesOfBackpacksInBackpack) {
            initSubBackpacksHandler();
            return new InceptionInventoryHandler(inventory, getInventoryOrder(), subBackpacksHandler);
        }
        return inventory;

    }

    private void initSubBackpacksHandler() {
        subBackpacksHandler = new SubBackpacksHandler(storageWrapper.getInventoryHandler());
    }

    @Override
    public IUpgradeWrapperAccessor wrapAccessor(IUpgradeWrapperAccessor upgradeWrapperAccessor) {
        if (GeneralConfig.upgradesInContainedBackpacksAreFunctional) {
            initSubBackpacksHandler();
            return new InceptionWrapperAccessor(storageWrapper, subBackpacksHandler);
        }
        return upgradeWrapperAccessor;
    }

    @Override
    @Nullable
    public IStorageFluidHandler wrapHandler(@Nullable IStorageFluidHandler fluidHandler, ItemStack backpack) {
        if (GeneralConfig.upgradesInContainedBackpacksAreFunctional) {
            initSubBackpacksHandler();
            return new InceptionFluidHandler(fluidHandler, backpack, getInventoryOrder(), subBackpacksHandler);
        }
        return fluidHandler;
    }

    @Override
    @Nullable
    public IEnergyStorage wrapStorage(@Nullable IEnergyStorage energyStorage) {
        if (GeneralConfig.upgradesInContainedBackpacksAreFunctional) {
            initSubBackpacksHandler();
            return new InceptionEnergyStorage(energyStorage, getInventoryOrder(), subBackpacksHandler);
        }
        return energyStorage;
    }
}
