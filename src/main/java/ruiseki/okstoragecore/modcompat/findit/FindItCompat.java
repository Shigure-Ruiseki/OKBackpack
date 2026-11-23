package ruiseki.okstoragecore.modcompat.findit;

import com.gtnh.findit.FindIt;

import ruiseki.okcore.enums.Mods;
import ruiseki.okcore.modcompat.IModCompat;

public class FindItCompat implements IModCompat {

    public FindItCompat() {

    }

    @Override
    public String getModID() {
        return Mods.FindIt.modid;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "FindIt compatibility.";
    }

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            FindIt.INSTANCE.pluginsList.add(new StorageProvider());
        }
    }
}
