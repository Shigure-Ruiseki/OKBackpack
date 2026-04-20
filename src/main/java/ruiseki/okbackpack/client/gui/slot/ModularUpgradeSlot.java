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

    protected final IStorageWrapper wrapper;

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
        return getBlockedTakeResult(player) == null;
    }

    @Nullable
    public UpgradeSlotChangeResult getBlockedTakeResult(EntityPlayer player) {
        ItemStack current = this.getStack();
        if (current == null) return null;

        // Admin infinity upgrade: only admins can remove/replace.
        if (current.getItem() instanceof ItemInfinityUpgrade) {
            if (!InfinityUpgradeWrapper.isAdmin(player)) {
                ItemStack cursor = player.inventory.getItemStack();
                if (cursor == null || !(cursor.getItem() instanceof ItemInfinityUpgrade
                    || cursor.getItem() instanceof ItemSurvivalInfinityUpgrade)) {
                    return null;
                }
            }
        }

        ItemStack cursor = player.inventory.getItemStack();
        int slot = getSlotIndex();

        if (cursor == null) {
            UpgradeSlotChangeResult result = wrapper.getRemoveUpgradeResult(slot);
            return result.isSuccessful() ? null : result;
        }

        UpgradeSlotChangeResult replaceResult = wrapper.getReplaceUpgradeResult(slot, cursor);
        return replaceResult.isSuccessful() ? null : replaceResult;
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

        UpgradeSlotChangeResult result = upgradeItem.canAddUpgradeTo(wrapper, stack, slot);
        if (!result.isSuccessful()) {
            return false;
        }

        if (!wrapper.canAddUpgrade(slot, stack)) {
            return false;
        }

        ItemStack current = getStack();
        if (current == null) {
            return true;
        }

        return wrapper.canReplaceUpgrade(slot, stack);
    }

    @Nullable
    public UpgradeSlotChangeResult getBlockedInsertResult(@Nullable ItemStack stack) {
        if (stack == null) return null;

        Item item = stack.getItem();
        int slot = getSlotIndex();

        if (!(item instanceof IUpgradeItem<?>upgradeItem)) {
            return null;
        }

        UpgradeSlotChangeResult result = upgradeItem.canAddUpgradeTo(wrapper, stack, slot);
        if (!result.isSuccessful()) {
            return result;
        }

        if (!wrapper.canAddUpgrade(slot, stack)) {
            return null;
        }

        ItemStack current = getStack();
        if (current == null) {
            return null;
        }

        UpgradeSlotChangeResult replaceResult = wrapper.getReplaceUpgradeResult(slot, stack);
        return replaceResult.isSuccessful() ? null : replaceResult;
    }
}
