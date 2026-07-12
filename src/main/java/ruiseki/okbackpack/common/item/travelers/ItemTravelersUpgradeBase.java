package ruiseki.okbackpack.common.item.travelers;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public abstract class ItemTravelersUpgradeBase<T extends IUpgradeWrapper> extends ItemUpgrade<T> {

    private final String[] tooltipKeys;

    protected ItemTravelersUpgradeBase(String... tooltipKeys) {
        super();
        this.tooltipKeys = tooltipKeys;
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        for (String tooltipKey : tooltipKeys) {
            list.add(LangHelpers.localize(tooltipKey));
        }
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlotsByWrapperType(wrapper, targetSlot, ITravelersUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult.failOnlySingleAllowed(
                conflicts,
                LangHelpers.localize("item.travelers_upgrade.name"),
                wrapper.getInventoryName());
        }
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }
}
