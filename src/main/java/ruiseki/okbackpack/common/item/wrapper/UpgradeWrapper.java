package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import lombok.Getter;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class UpgradeWrapper implements IUpgrade {

    @Getter
    protected final ItemStack upgrade;

    public UpgradeWrapper(ItemStack upgrade) {
        this.upgrade = upgrade;
    }

    @Override
    public void setTabOpened(boolean opened) {
        ItemNBTHelpers.setBoolean(upgrade, TAB_STATE_TAG, opened);
    }

    @Override
    public boolean isTabOpened() {
        return ItemNBTHelpers.getBoolean(upgrade, TAB_STATE_TAG, false);
    }

    @Override
    public String getSettingLangKey() {
        return "";
    }

}
