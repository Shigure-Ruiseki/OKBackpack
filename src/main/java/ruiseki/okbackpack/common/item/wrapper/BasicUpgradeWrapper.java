package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class BasicUpgradeWrapper extends UpgradeWrapper implements IBasicFilterable, IToggleable {

    protected UpgradeItemStackHandler handler;
    private boolean filterItemsCached = false;

    public BasicUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
        handler = new UpgradeItemStackHandler(9) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(IBasicFilterable.FILTER_ITEMS_TAG, this.serializeNBT());
            }
        };
    }

    @Override
    public FilterType getFilterType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, FILTER_TYPE_TAG, FilterType.BLACKLIST.ordinal());
        FilterType[] types = FilterType.values();
        if (ordinal < 0 || ordinal >= types.length) {
            return FilterType.BLACKLIST;
        }
        return types[ordinal];
    }

    @Override
    public void setFilterType(FilterType type) {
        if (type == null) {
            type = FilterType.BLACKLIST;
        }
        ItemNBTHelpers.setInt(upgrade, FILTER_TYPE_TAG, type.ordinal());
    }

    @Override
    public UpgradeItemStackHandler getFilterItems() {
        if (!filterItemsCached) {
            NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, FILTER_ITEMS_TAG, false);
            if (handlerTag != null) {
                handler.deserializeNBT(handlerTag);
            }
            filterItemsCached = true;
        }
        return handler;
    }

    @Override
    public void setFilterItems(UpgradeItemStackHandler handler) {
        if (handler != null) {
            ItemNBTHelpers.setCompound(upgrade, FILTER_ITEMS_TAG, handler.serializeNBT());
            filterItemsCached = false;
        }
    }

    @Override
    public boolean checkFilter(ItemStack check) {
        return isEnabled() && IBasicFilterable.super.checkFilter(check);
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }
}
