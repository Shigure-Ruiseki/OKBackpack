package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import lombok.Getter;
import lombok.Setter;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.common.item.infinity.InfinityUpgradeWrapper;
import ruiseki.okbackpack.common.item.infinity.ItemInfinityUpgrade;
import ruiseki.okbackpack.common.item.infinity.ItemSurvivalInfinityUpgrade;

public class ModularUpgradeSlot extends ModularSlot {

    private final IStorageWrapper wrapper;

    @Getter
    @Setter
    @Nullable
    private UpgradeSlotChangeResult lastChangeResult;

    public ModularUpgradeSlot(IStorageWrapper wrapper, int index) {
        super(wrapper.getUpgradeHandler(), index);
        this.wrapper = wrapper;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack current = this.getStack();
        if (current == null) return true;

        // Admin infinity upgrade: only admins can remove/replace
        if (current.getItem() instanceof ItemInfinityUpgrade) {
            if (!InfinityUpgradeWrapper.isAdmin(player)) {
                // Non-admin can only swap with another infinity variant
                ItemStack cursor = player.inventory.getItemStack();
                if (cursor == null || !(cursor.getItem() instanceof ItemInfinityUpgrade
                    || cursor.getItem() instanceof ItemSurvivalInfinityUpgrade)) {
                    return false;
                }
            }
        }

        ItemStack cursor = player.inventory.getItemStack();
        int slot = getSlotIndex();

        // cursor empty → remove
        if (cursor == null) {
            UpgradeSlotChangeResult result = wrapper.getRemoveUpgradeResult(slot);
            if (!result.isSuccessful()) {
                lastChangeResult = result;
                return false;
            }
            return true;
        }

        // cursor not empty → replace
        UpgradeSlotChangeResult replaceResult = wrapper.getReplaceUpgradeResult(slot, cursor);
        if (!replaceResult.isSuccessful()) {
            lastChangeResult = replaceResult;
            return false;
        }
        return true;
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        if (stack == null) return false;

        Item item = stack.getItem();
        int slot = getSlotIndex();

        if (!(item instanceof IUpgradeItem<?>upgradeItem)) {
            return false;
        }

        // check IUpgradeItem.canAddUpgradeTo
        UpgradeSlotChangeResult result = upgradeItem.canAddUpgradeTo(wrapper, stack, slot);
        lastChangeResult = result;
        if (!result.isSuccessful()) {
            return false;
        }

        // check global upgrade rules
        if (!wrapper.canAddUpgrade(slot, stack)) {
            return false;
        }

        // simulate replace (slot currently has something?)
        ItemStack current = getStack();

        if (current == null) {
            lastChangeResult = UpgradeSlotChangeResult.success();
            return true;
        }

        boolean canReplace = wrapper.canReplaceUpgrade(slot, stack);
        if (!canReplace) {
            lastChangeResult = null;
        }
        return canReplace;
    }
}
