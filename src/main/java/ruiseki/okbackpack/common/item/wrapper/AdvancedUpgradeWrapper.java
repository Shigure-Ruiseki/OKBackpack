package ruiseki.okbackpack.common.item.wrapper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedUpgradeWrapper extends UpgradeWrapper implements IAdvancedFilterable, IToggleable {

    protected UpgradeItemStackHandler handler;

    public AdvancedUpgradeWrapper(ItemStack upgrade) {
        super(upgrade);
        this.handler = new UpgradeItemStackHandler(16) {

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
        NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, FILTER_ITEMS_TAG, false);
        if (handlerTag != null) {
            handler.deserializeNBT(handlerTag);
        }
        return handler;
    }

    @Override
    public void setFilterItems(UpgradeItemStackHandler handler) {
        if (handler != null) {
            ItemNBTHelpers.setCompound(upgrade, FILTER_ITEMS_TAG, handler.serializeNBT());
        }
    }

    @Override
    public MatchType getMatchType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, MATCH_TYPE_TAG, MatchType.ITEM.ordinal());
        MatchType[] types = MatchType.values();
        if (ordinal < 0 || ordinal >= types.length) {
            return MatchType.ITEM;
        }
        return types[ordinal];
    }

    @Override
    public void setMatchType(MatchType matchType) {
        if (matchType == null) {
            matchType = MatchType.ITEM;
        }
        ItemNBTHelpers.setInt(upgrade, MATCH_TYPE_TAG, matchType.ordinal());
    }

    @Override
    public List<String> getOreDictEntries() {
        NBTTagCompound listTag = ItemNBTHelpers.getCompound(upgrade, ORE_DICT_LIST_TAG, false);
        List<String> list = new ArrayList<>();
        if (listTag != null) {
            for (String key : listTag.func_150296_c()) {
                list.add(listTag.getString(key));
            }
        }
        return list;
    }

    @Override
    public void setOreDictEntries(List<String> entries) {
        if (entries == null) return;
        NBTTagCompound listTag = new NBTTagCompound();
        for (int i = 0; i < entries.size(); i++) {
            listTag.setString("e" + i, entries.get(i));
        }
        ItemNBTHelpers.setCompound(upgrade, ORE_DICT_LIST_TAG, listTag);
    }

    @Override
    public boolean isIgnoreDurability() {
        return ItemNBTHelpers.getBoolean(upgrade, IGNORE_DURABILITY_TAG, true);
    }

    @Override
    public void setIgnoreDurability(boolean ignore) {
        ItemNBTHelpers.setBoolean(upgrade, IGNORE_DURABILITY_TAG, ignore);
    }

    @Override
    public boolean isIgnoreNBT() {
        return ItemNBTHelpers.getBoolean(upgrade, IGNORE_NBT_TAG, true);
    }

    @Override
    public void setIgnoreNBT(boolean ignore) {
        ItemNBTHelpers.setBoolean(upgrade, IGNORE_NBT_TAG, ignore);
    }

    @Override
    public boolean checkFilter(ItemStack check) {
        return isEnabled() && IAdvancedFilterable.super.checkFilter(check);
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
