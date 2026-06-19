package ruiseki.okbackpack.compat.bauble;

import ruiseki.okcore.helper.BaublesHelpers;
import ruiseki.okcore.init.IInitListener;

public class BaubleCompat implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step != Step.PREINIT) return;
        BaublesHelpers.assignSlotsUpToMinimum("body", 1);
    }
}
