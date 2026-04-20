package ruiseki.okbackpack.common.item.travelers.slime;

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

public class ItemSlimeUpgrade extends ItemUpgrade<SlimeUpgradeWrapper> {

    public ItemSlimeUpgrade() {
        super("slime_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "slime_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.slime_upgrade"));
        list.add(LangHelpers.localize("tooltip.backpack.slime_upgrade.1"));
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
    public SlimeUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new SlimeUpgradeWrapper(stack, storage, consumer);
    }
}
