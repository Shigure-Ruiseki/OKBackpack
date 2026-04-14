package ruiseki.okbackpack.common.item.arcane;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IArcaneCraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class ArcaneCraftingUpgradeWrapper extends UpgradeWrapperBase implements IArcaneCraftingUpgrade {

    protected BaseItemStackHandler handler;
    private boolean hasWand;
    private String missingResearch;
    private String missingResearchName;

    public ArcaneCraftingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
        handler = new BaseItemStackHandler(11) {

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(STORAGE_TAG, this.serializeNBT());
                save();
            }
        };
        NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, STORAGE_TAG, false);
        if (handlerTag != null) handler.deserializeNBT(handlerTag);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.arcane_crafting_settings";
    }

    @Override
    public BaseItemStackHandler getStorage() {
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
        save();
    }

    @Override
    public boolean isUseBackpack() {
        return ItemNBTHelpers.getBoolean(upgrade, USE_BACKPACK_TAG, false);
    }

    @Override
    public void setUseBackpack(boolean used) {
        ItemNBTHelpers.setBoolean(upgrade, USE_BACKPACK_TAG, used);
        save();
    }

    @Override
    public Map<String, Integer> getRequiredAspects() {
        Map<String, Integer> result = new LinkedHashMap<>();
        NBTTagCompound tag = ItemNBTHelpers.getCompound(upgrade, ARCANE_ASPECTS_TAG, false);
        if (tag == null) return result;
        for (String aspectTag : tag.func_150296_c()) {
            result.put(aspectTag, tag.getInteger(aspectTag));
        }
        return result;
    }

    @Override
    public void setRequiredAspects(Map<String, Integer> aspects) {
        NBTTagCompound tag = new NBTTagCompound();
        if (aspects != null) {
            for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
                tag.setInteger(entry.getKey(), entry.getValue());
            }
        }
        ItemNBTHelpers.getNBT(upgrade)
            .setTag(ARCANE_ASPECTS_TAG, tag);
        save();
    }

    @Override
    public boolean hasWand() {
        return hasWand;
    }

    @Override
    public void setHasWand(boolean hasWand) {
        this.hasWand = hasWand;
    }

    @Override
    public String getMissingResearch() {
        return missingResearch;
    }

    @Override
    public void setMissingResearch(String researchKey) {
        this.missingResearch = researchKey;
    }

    @Override
    public String getMissingResearchName() {
        return missingResearchName;
    }

    @Override
    public void setMissingResearchName(String name) {
        this.missingResearchName = name;
    }

    @Override
    public String getCraftingInfoKey() {
        return "arcane_info";
    }
}
