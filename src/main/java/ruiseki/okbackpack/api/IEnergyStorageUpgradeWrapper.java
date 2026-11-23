package ruiseki.okbackpack.api;

import org.jetbrains.annotations.Nullable;

import cofh.api.energy.IEnergyStorage;

public interface IEnergyStorageUpgradeWrapper {

    @Nullable
    IEnergyStorage wrapStorage(@Nullable IEnergyStorage energyStorage);
}
