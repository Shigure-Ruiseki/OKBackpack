package ruiseki.backpack.modcompat.jfmuy;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;

public class JFMUYModCompat implements IModCompat {

    /**
     * If the modcompat can be used.
     */
    public static boolean canBeUsed = false;

    public JFMUYModCompat() {

    }

    @Override
    public String getModID() {
        return Mods.JFMUY.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Integration for JFMUY upgrades.";
    }

    @Override
    public void onInit(Step initStep) {
        if (initStep == Step.PREINIT) {
            canBeUsed = OKBackpack._instance.getModCompatLoader()
                .shouldLoadModCompat(this);
        }
    }
}
