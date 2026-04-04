package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;

public interface ISmeltingUpgrade extends ITickable, IToggleable {

    String SMELTING_PROGRESS_TAG = "SmeltProgress";
    String SMELTING_FUEL_PROGRESS_TAG = "FuelProgress";
    String SMELTING_FUEL_TOTAL_TAG = "FuelTotal";
    String FUEL_FILTER_TAG = "FuelFilter";

    BaseItemStackHandler getSmeltingInventory();

    ItemStack getSmeltingResult(ItemStack input);

    int getSmeltTime();

    int getSmeltProgress();

    void setSmeltProgress(int progress);

    int getFuelProgress();

    void setFuelProgress(int progress);

    int getFuelTotal();

    void setFuelTotal(int total);

    boolean isBurning();

    default BaseItemStackHandler getFuelFilterItems() {
        return null;
    }

    default boolean checkFuelFilter(ItemStack stack) {
        return true;
    }
}
