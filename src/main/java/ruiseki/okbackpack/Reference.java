package ruiseki.okbackpack;

public class Reference {

    public static final String MOD_ID = Tags.MOD_ID;
    public static final String MOD_NAME = Tags.MOD_NAME;
    public static final String VERSION = Tags.VERSION;
    public static final String DEPENDENCIES = "required-after:gtnhlib@[0.11.9,);"
        + "required-after:modularui2@[2.3.73-1.7.10,);"
        + "required-after:okcore@[26.06.26.3,);"
        + "after:Baubles;"
        + "after:TConstruct;"
        + "after:angelica;"
        + "after:NotEnoughItems;"
        + "after:Waila;";
    public static final String UPDATE_URL = "https://raw.githubusercontent.com/Shigure-Ruiseki/OKBackpack/master/version/version.json";

    public static final String PROXY_COMMON = Tags.MOD_GROUP + ".CommonProxy";
    public static final String PROXY_CLIENT = Tags.MOD_GROUP + ".ClientProxy";
    public static final String GUI_FACTORY = Tags.MOD_GROUP + ".config.GuiConfigFactory";

    public static final String PREFIX_MOD = MOD_ID + ":";
}
