package ruiseki.okbackpack.modcompat.tic;

import org.apache.logging.log4j.Level;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.enums.Mods;

public class TConstructTabCompat {

    private static boolean registered;

    private TConstructTabCompat() {}

    public static void registerClientTabs() {
        if (registered || !Mods.TConstruct.isModLoaded()) {
            return;
        }

        registered = true;
        try {
            TConstructTabIntegration.registerTab();
            TConstructTabIntegration.registerOrderingHook();
        } catch (LinkageError | RuntimeException e) {
            OKBackpack.okLog(Level.ERROR, "Failed to register TConstruct inventory tab: " + e);
        }
    }
}
