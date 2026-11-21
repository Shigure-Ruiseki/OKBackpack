package ruiseki.okstoragecore.upgrades;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.NBTHelpers;

public class FluidFilterLogicBase {

    protected final ItemStack upgrade;
    protected final Consumer<ItemStack> saveHandler;
    protected final String parentTagKey;

    private boolean allowListDefault = false;

    public FluidFilterLogicBase(ItemStack upgrade, Consumer<ItemStack> saveHandler, String parentTagKey) {
        this.upgrade = upgrade;
        this.saveHandler = saveHandler;
        this.parentTagKey = parentTagKey;
    }

    public String getParentTagKey() {
        return parentTagKey;
    }

    protected void save() {
        saveHandler.accept(upgrade);
    }

    public void setAllowListDefault(boolean allowListDefault) {
        this.allowListDefault = allowListDefault;
    }

    public void setAllowList(boolean allowList) {
        NBTHelpers.setBoolean(upgrade, parentTagKey, "isAllowList", allowList);
        save();
    }

    public boolean isAllowList() {
        return NBTHelpers.getBoolean(upgrade, parentTagKey, "isAllowList")
            .orElse(allowListDefault);
    }
}
