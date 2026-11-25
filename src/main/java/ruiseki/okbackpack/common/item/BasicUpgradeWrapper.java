package ruiseki.okbackpack.common.item;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class BasicUpgradeWrapper extends UpgradeWrapperBase implements IBasicFilterable, IToggleable {

    protected BaseItemStackHandler handler;

    public BasicUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
        handler = new BaseItemStackHandler(9) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(IBasicFilterable.FILTER_ITEMS_TAG, this.serializeNBT());
                save();
            }
        };
        NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, FILTER_ITEMS_TAG, false);
        if (handlerTag != null) handler.deserializeNBT(handlerTag);
    }

    @Override
    public FilterType getFilterType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, FILTER_TYPE_TAG, FilterType.BLACKLIST.ordinal());
        FilterType[] types = FilterType.values();
        if (ordinal < 0 || ordinal >= types.length) return FilterType.BLACKLIST;
        return types[ordinal];
    }

    @Override
    public void setFilterType(FilterType type) {
        if (type == null) type = FilterType.BLACKLIST;
        ItemNBTHelpers.setInt(upgrade, FILTER_TYPE_TAG, type.ordinal());
        save();
    }

    @Override
    public BaseItemStackHandler getFilterItems() {
        return handler;
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
        save();
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }
}
