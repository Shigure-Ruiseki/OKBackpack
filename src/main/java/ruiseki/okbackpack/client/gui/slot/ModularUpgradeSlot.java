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
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.common.item.ItemUpgrade;

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

        ItemStack cursor = player.inventory.getItemStack();
        int slot = getSlotIndex();

        // cursor empty → remove
        if (cursor == null) {
            return wrapper.canRemoveUpgrade(slot);
        }

        // cursor not empty → replace
        return wrapper.canReplaceUpgrade(slot, cursor);
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

        if (!(item instanceof ItemUpgrade<?>upgradeItem)) {
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
