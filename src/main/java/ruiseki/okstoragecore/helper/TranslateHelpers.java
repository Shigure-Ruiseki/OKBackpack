package ruiseki.okstoragecore.helper;

import ruiseki.okcore.helper.LangHelpers;

public class TranslateHelpers {

    public static String error(String key, Object... params) {
        return LangHelpers.localize("gui.okstoragecore.error." + key, params);
    }
}
