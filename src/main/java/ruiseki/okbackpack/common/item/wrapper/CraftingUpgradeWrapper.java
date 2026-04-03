package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class CraftingUpgradeWrapper extends UpgradeWrapperBase implements ICraftingUpgrade {

    protected UpgradeItemStackHandler handler;

    public CraftingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
        handler = new UpgradeItemStackHandler(10) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(ICraftingUpgrade.STORAGE_TAG, this.serializeNBT());
            }
        };
        NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, STORAGE_TAG, false);
        if (handlerTag != null) handler.deserializeNBT(handlerTag);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.crafting_settings";
    }

    @Override
    public UpgradeItemStackHandler getStorage() {
        return handler;
    }

    @Override
    public CraftingDestination getCraftingDes() {
        int ordinal = ItemNBTHelpers
            .getInt(upgrade, CRAFTING_DEST_TAG, IBasicFilterable.FilterType.WHITELIST.ordinal());
        CraftingDestination[] types = CraftingDestination.values();
        if (ordinal < 0 || ordinal >= types.length) return CraftingDestination.BACKPACK;
        return types[ordinal];
    }

    @Override
    public void setCraftingDes(CraftingDestination type) {
        if (type == null) type = CraftingDestination.BACKPACK;
        ItemNBTHelpers.setInt(upgrade, CRAFTING_DEST_TAG, type.ordinal());
        markDirty();
    }

    @Override
    public boolean isUseBackpack() {
        return ItemNBTHelpers.getBoolean(upgrade, USE_BACKPACK_TAG, false);
    }

    @Override
    public void setUseBackpack(boolean used) {
        ItemNBTHelpers.setBoolean(upgrade, USE_BACKPACK_TAG, used);
        markDirty();
    }

}
