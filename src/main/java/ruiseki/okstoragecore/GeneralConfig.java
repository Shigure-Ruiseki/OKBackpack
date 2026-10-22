package ruiseki.okstoragecore;

import org.apache.logging.log4j.Level;

import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.DummyConfig;
import ruiseki.okcore.tracking.Versions;

public class GeneralConfig extends DummyConfig {

    @ConfigurableProperty(
        category = "core",
        comment = "Config version for " + Reference.MOD_NAME + ".\nDO NOT EDIT MANUALLY!")
    public static String version = Reference.MOD_VERSION;

    @ConfigurableProperty(category = "core", comment = "If the version checker should be enabled.")
    public static boolean useVersionChecker = true;

    public GeneralConfig() {
        super(OKStorageCore._instance, true, "general", null);
    }

    @Override
    public void onRegistered() {
        if (!version.equals(Reference.MOD_VERSION)) {
            getMod().log(
                Level.WARN,
                "The config file of " + Reference.MOD_NAME
                    + " is out of date and might cause problems, "
                    + "please remove it so it can be regenerated.");
        }

        if (useVersionChecker) {
            Versions.registerMod(getMod(), OKStorageCore._instance, Reference.VERSION_URL);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
