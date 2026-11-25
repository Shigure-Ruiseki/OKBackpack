package ruiseki.okbackpack.api.wrapper;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

public interface IUpgradeWrapper {

    String TAB_STATE_TAG = "TabState";

    void setTabOpened(boolean opened);

    boolean isTabOpened();

    String getSettingLangKey();

    ItemStack getUpgradeStack();

    void onAdded();

    void onBeforeRemoved();

    default List<String> getTooltipLines() {
        return Collections.emptyList();
    }
}
