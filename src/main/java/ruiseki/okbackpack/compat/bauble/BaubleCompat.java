package ruiseki.okbackpack.compat.bauble;

import baubles.api.expanded.BaubleExpandedSlots;
import ruiseki.okcore.init.IInitListener;

public class BaubleCompat implements IInitListener {

    public static int[] backpackSlotIDs;

    @Override
    public void onInit(Step step) {
        if (step == Step.PREINIT) {
            BaubleExpandedSlots.tryAssignSlotsUpToMinimum(BaubleExpandedSlots.bodyType, 1);
        }
        if (step == Step.POSTINIT) {
            backpackSlotIDs = BaubleExpandedSlots.getIndexesOfAssignedSlotsOfType(BaubleExpandedSlots.bodyType);
        }
    }
}
