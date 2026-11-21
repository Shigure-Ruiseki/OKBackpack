package ruiseki.okstoragecore;

import ruiseki.okcore.config.ConfigHandler;
import ruiseki.okstoragecore.capabilities.CapabilityStorageWrapperConfig;

public class Configs {

    public static void register(ConfigHandler configHandler) {
        configHandler.add(new CapabilityStorageWrapperConfig());
    }
}
