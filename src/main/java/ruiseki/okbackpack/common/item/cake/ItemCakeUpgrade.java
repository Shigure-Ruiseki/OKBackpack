package ruiseki.okbackpack.common.item.cake;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemCakeUpgrade extends ItemUpgrade<CakeUpgradeWrapper> {

    public ItemCakeUpgrade() {
        super("cake_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "cake_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.cake_upgrade"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlots(wrapper, targetSlot, ItemCakeUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult
                .failOnlySingleAllowed(conflicts, upgradeStack.getDisplayName(), wrapper.getDisplayName());
        }
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public CakeUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new CakeUpgradeWrapper(stack, storage, consumer);
    }
}
