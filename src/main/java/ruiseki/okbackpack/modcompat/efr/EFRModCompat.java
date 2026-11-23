package ruiseki.okbackpack.modcompat.efr;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.modcompat.efr.item.ItemAutoBlastingUpgradeConfig;
import ruiseki.okbackpack.modcompat.efr.item.ItemAutoSmokingUpgradeConfig;
import ruiseki.okbackpack.modcompat.efr.item.ItemBlastingUpgradeConfig;
import ruiseki.okbackpack.modcompat.efr.item.ItemSmokingUpgradeConfig;
import ruiseki.okcore.config.ConfigHandler;
import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;

public class EFRModCompat implements IModCompat {

    /**
     * If the modcompat can be used.
     */
    public static boolean canBeUsed = false;

    public EFRModCompat() {

    }

    @Override
    public String getModID() {
        return Mods.EtFuturum.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Integration for EtFuturum upgrades.";
    }

    @Override
    public void onInit(Step initStep) {
        if (initStep == Step.PREINIT) {
            canBeUsed = OKBackpack._instance.getModCompatLoader()
                .shouldLoadModCompat(this);

            ConfigHandler configHandler = OKBackpack._instance.getConfigHandler();
            configHandler.add(new ItemSmokingUpgradeConfig());
            configHandler.add(new ItemBlastingUpgradeConfig());
            configHandler.add(new ItemAutoSmokingUpgradeConfig());
            configHandler.add(new ItemAutoBlastingUpgradeConfig());
        }
    }
}
