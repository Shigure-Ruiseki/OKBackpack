package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;

public interface ISmeltingUpgrade extends ITickable, IToggleable, IProgressable, IStorageUpgrade {

    String COOK_TIME_TAG = "CookTime";
    String BURN_TIME_TAG = "BurnTime";
    String BURN_TIME_TOTAL_TAG = "BurnTimeTotal";
    String FUEL_FILTER_TAG = "FuelFilter";

    ItemStack getSmeltingResult(ItemStack input);

    int getTotalCookTime();

    int getCookTime();

    void setCookTime(int progress);

    int getBurnTime();

    void setBurnTime(int progress);

    int getTotalBurnTime();

    void setTotalBurnTime(int total);

    float getBurnProgress();

    boolean isBurning();

    default BaseItemStackHandler getFuelFilterItems() {
        return null;
    }

    default boolean checkFuelFilter(ItemStack stack) {
        return true;
    }
}
