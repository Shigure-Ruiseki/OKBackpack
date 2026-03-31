package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class UpgradeWrapper implements IUpgradeWrapper, IDirtable {

    protected final ItemStack upgrade;
    protected final IStorageWrapper storage;

    public UpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        this.upgrade = upgrade;
        this.storage = storage;
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

    @Override
    public boolean isDirty() {
        return ItemNBTHelpers.getBoolean(upgrade, DIRTY_TAG, false);
    }

    @Override
    public void markDirty() {
        ItemNBTHelpers.setBoolean(upgrade, DIRTY_TAG, true);
    }

    @Override
    public void markClean() {
        ItemNBTHelpers.setBoolean(upgrade, DIRTY_TAG, false);
    }

    @Override
    public void setDirty(boolean value) {
        ItemNBTHelpers.setBoolean(upgrade, DIRTY_TAG, value);
    }
}
