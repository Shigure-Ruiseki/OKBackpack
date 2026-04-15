package ruiseki.okbackpack.common.item.cactus;

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

public class ItemCactusUpgrade extends ItemUpgrade<CactusUpgradeWrapper> {

    public ItemCactusUpgrade() {
        super("cactus_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "cactus_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.cactus_upgrade"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem.findConflictSlots(wrapper, targetSlot, ItemCactusUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult
                .failOnlySingleAllowed(conflicts, upgradeStack.getDisplayName(), wrapper.getDisplayName());
        }
        return super.canAddUpgradeTo(wrapper, upgradeStack, targetSlot);
    }

    @Override
    public CactusUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new CactusUpgradeWrapper(stack, storage, consumer);
    }
}
