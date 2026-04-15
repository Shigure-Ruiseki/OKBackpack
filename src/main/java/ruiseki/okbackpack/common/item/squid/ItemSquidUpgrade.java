package ruiseki.okbackpack.common.item.travelers.squid;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemSquidUpgrade extends ItemUpgrade<SquidUpgradeWrapper> {

    public ItemSquidUpgrade() {
        super("squid_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "squid_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.squid_upgrade"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlotsByWrapperType(wrapper, targetSlot, ITravelersUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult.failOnlySingleAllowed(
                conflicts,
                LangHelpers.localize("item.travelers_upgrade.name"),
                wrapper.getDisplayName());
        }
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public SquidUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new SquidUpgradeWrapper(stack, storage, consumer);
    }
}
