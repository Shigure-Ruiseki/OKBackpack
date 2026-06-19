package ruiseki.okbackpack.compat.findit;

import com.gtnh.findit.FindIt;

import ruiseki.okbackpack.compat.Mods;

public class FindItCompat {

    public static void registerProvider() {
        if (!Mods.FindIt.isModLoaded()) return;
        FindIt.INSTANCE.pluginsList.add(new BackpackProvider());
    }
}
