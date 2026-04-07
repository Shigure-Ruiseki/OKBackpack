package ruiseki.okbackpack.api.wrapper;

import net.minecraft.entity.player.EntityPlayer;

public interface IAnvilUpgrade extends IStorageUpgrade {

    String ANVIL_INV_TAG = "AnvilInv";
    String MAXIMUM_COST_TAG = "MaximumCost";
    String REPAIRED_ITEM_NAME_TAG = "RepairedItemName";
    String STACK_SIZE_TO_USE_TAG = "StackSizeToUse";

    int getMaximumCost();

    String getRepairedItemName();

    void setRepairedItemName(String name);

    void updateRepairOutput();

    void onTakeOutput(EntityPlayer player);

    int getStackSizeToBeUsedInRepair();
}
