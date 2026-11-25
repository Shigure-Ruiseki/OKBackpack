package ruiseki.okbackpack.common.item;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IDirtable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class UpgradeWrapperBase implements IUpgradeWrapper, IDirtable {

    protected final ItemStack upgrade;
    protected final IStorageWrapper storage;
    protected final Consumer<ItemStack> upgradeConsumer;
    protected boolean dirty;

    public UpgradeWrapperBase(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        this.upgrade = upgrade;
        this.storage = storage;
        this.upgradeConsumer = upgradeConsumer;
    }

    @Override
    public void setTabOpened(boolean opened) {
        ItemNBTHelpers.setBoolean(upgrade, TAB_STATE_TAG, opened);
        markDirty();
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
    public ItemStack getUpgradeStack() {
        return upgrade;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onBeforeRemoved() {

    }

    public void save() {
        upgradeConsumer.accept(upgrade);
        storage.markDirty();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public void markClean() {
        this.dirty = false;
    }

    @Override
    public void setDirty(boolean value) {
        this.dirty = value;
    }
}
