package ruiseki.storagecore.helper;

import ruiseki.okcore.helper.LangHelpers;

public class TranslateHelpers {

    public static String error(String key, Object... params) {
        return LangHelpers.localize("gui.storage.error." + key, params);
    }
}
