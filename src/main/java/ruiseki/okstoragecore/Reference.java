package ruiseki.okstoragecore;

import ruiseki.okbackpack.Tags;

public class Reference {

    public static final String MOD_ID = "okstoragecore";
    public static final String MOD_NAME = "OK Storage Core";
    public static final String MOD_VERSION = Tags.VERSION;
    public static final String MOD_DEPENDENCIES = "required-after:gtnhlib@[0.11.48,);"
        + "required-after:modularui2@[2.3.90-1.7.10,);"
        + "required-after:okcore;";
    public static final String VERSION_URL = "";

    public static final String PROXY_COMMON = "ruiseki.okstoragecore.proxy.CommonProxy";
    public static final String PROXY_CLIENT = "ruiseki.okstoragecore.proxy.ClientProxy";
    public static final String GUI_FACTORY = "ruiseki.okstoragecore.GuiConfigOverview$ExtendedConfigGuiFactory";

    public static final String PREFIX_MOD = MOD_ID + ":";
}
