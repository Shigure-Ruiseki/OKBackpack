package ruiseki.okbackpack.api.wrapper;

public interface IBatteryUpgrade extends IStorageUpgrade, ITickable {

    int getEnergyStored();

    int getMaxEnergyStored();

    int receiveEnergy(int maxReceive, boolean simulate);

    int extractEnergy(int maxExtract, boolean simulate);

    boolean canExtract();

    boolean canReceive();

    int getMaxTransfer();

    float getChargeRatio();
}
