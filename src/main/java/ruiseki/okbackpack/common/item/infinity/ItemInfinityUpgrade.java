package ruiseki.okbackpack.common.item.infinity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okbackpack.common.item.ItemUpgrade;
import ruiseki.okcore.helper.LangHelpers;

public class ItemInfinityUpgrade extends ItemUpgrade<InfinityUpgradeWrapper> {

    public ItemInfinityUpgrade() {
        super("infinity_upgrade");
        setMaxStackSize(1);
        setTextureName(Reference.PREFIX_MOD + "infinity_upgrade");
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag) {
        list.add(LangHelpers.localize("tooltip.backpack.infinity_upgrade"));
        list.add(LangHelpers.localize("tooltip.backpack.infinity_upgrade.1"));
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(IStorageWrapper wrapper, ItemStack upgradeStack, int targetSlot) {
        int[] conflicts = IUpgradeItem
            .findConflictSlots(wrapper, targetSlot, ItemInfinityUpgrade.class, ItemSurvivalInfinityUpgrade.class);
        if (conflicts.length >= 1) {
            return UpgradeSlotChangeResult
                .failOnlySingleAllowed(conflicts, upgradeStack.getDisplayName(), wrapper.getDisplayName());
        }

        // All other upgrade slots must be empty
        UpgradeItemStackHandler handler = wrapper.getUpgradeHandler();
        List<Integer> occupiedSlots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (i == targetSlot) continue;
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null) {
                occupiedSlots.add(i);
            }
        }
        if (!occupiedSlots.isEmpty()) {
            return UpgradeSlotChangeResult.fail(
                "gui.backpack.error.add.any_upgrade_exists",
                occupiedSlots.stream()
                    .mapToInt(Integer::intValue)
                    .toArray());
        }

        return UpgradeSlotChangeResult.success();
    }

    @Override
    public InfinityUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage,
        Consumer<ItemStack> consumer) {
        return new InfinityUpgradeWrapper(stack, storage, consumer, true);
    }
}
