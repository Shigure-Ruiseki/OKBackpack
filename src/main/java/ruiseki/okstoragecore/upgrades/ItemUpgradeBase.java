package ruiseki.okstoragecore.upgrades;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.ItemBase;

public abstract class ItemUpgradeBase<T extends IUpgradeWrapper> extends ItemBase implements IUpgradeItem<T> {

    private final IUpgradeCountLimitConfig upgradeTypeLimitConfig;

    protected ItemUpgradeBase(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
        this.upgradeTypeLimitConfig = upgradeTypeLimitConfig;
    }

    @Override
    public int getUpgradesPerStorage(String storageType) {
        return upgradeTypeLimitConfig
            .getMaxUpgradesPerStorage(storageType, new ResourceLocation(Item.itemRegistry.getNameForObject(this)));
    }

    @Override
    public int getUpgradesInGroupPerStorage(String storageType) {
        if (getUpgradeGroup().isSolo()) {
            return Integer.MAX_VALUE;
        }

        return upgradeTypeLimitConfig.getMaxUpgradesInGroupPerStorage(storageType, getUpgradeGroup());
    }

    @Override
    public String getName() {
        return LangHelpers.localize(getUnlocalizedName());
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List<String> list, boolean flag) {
        super.addInformation(itemStack, entityPlayer, list, flag);
        if (itemStack.hasTagCompound()) {
            list.add(
                itemStack.getTagCompound()
                    .toString());
        }
    }
}
