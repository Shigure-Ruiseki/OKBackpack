package ruiseki.okbackpack.modcompat.bauble;

import ruiseki.okbackpack.helpers.BaublesHelpers;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;

public class BaubleModCompat implements IModCompat {

    @Override
    public String getModID() {
        return Mods.BaublesExpanded.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Bauble compatibility";
    }

    @Override
    public void onInit(Step step) {
        if (step == Step.PREINIT) {
            BaublesHelpers.assignSlotsUpToMinimum("body", 1);
        }
    }
}
