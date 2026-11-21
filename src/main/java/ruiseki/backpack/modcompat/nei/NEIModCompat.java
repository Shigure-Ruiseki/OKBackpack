package ruiseki.backpack.modcompat.nei;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;

public class NEIModCompat implements IModCompat {

    /**
     * If the modcompat can be used.
     */
    public static boolean canBeUsed = false;

    public NEIModCompat() {

    }

    @Override
    public String getModID() {
        return Mods.NotEnoughItems.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Integration for NEI recipe transfers.";
    }

    @Override
    public void onInit(Step initStep) {
        if (initStep == Step.PREINIT) {
            canBeUsed = OKBackpack._instance.getModCompatLoader()
                .shouldLoadModCompat(this);
        }
    }
}
