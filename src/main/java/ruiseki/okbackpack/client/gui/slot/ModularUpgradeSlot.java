package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.ItemInceptionUpgrade;
import ruiseki.okbackpack.common.item.ItemStackUpgrade;
import ruiseki.okbackpack.common.item.ItemUpgrade;

public class ModularUpgradeSlot extends ModularSlot {

    private final BackpackWrapper wrapper;

    public ModularUpgradeSlot(BackpackWrapper wrapper, int index) {
        super(wrapper.upgradeHandler, index);
        this.wrapper = wrapper;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack originalStack = this.getStack();
        if (originalStack == null) {
            return true;
        }

        ItemStack cursor = player.inventory.getItemStack();
        boolean cursorEmpty = (cursor == null);

        Item originalItem = originalStack.getItem();

        if (originalItem instanceof ItemStackUpgrade) {
            int slotIndex = getSlotIndex();

            if (cursorEmpty) {
                return wrapper.canReplaceStackUpgrade(slotIndex, null);
            }

            if (cursor.getItem() instanceof ItemStackUpgrade) {
                return wrapper.canReplaceStackUpgrade(slotIndex, cursor);
            }

            return wrapper.canReplaceStackUpgrade(slotIndex, null);
        }

        if (originalItem instanceof ItemInceptionUpgrade) {

            if (cursorEmpty) {
                return wrapper.canRemoveInceptionUpgrade();
            }

            if (!(cursor.getItem() instanceof ItemInceptionUpgrade)) {
                return wrapper.canRemoveInceptionUpgrade();
            }

            return true;
        }

        return true;
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }

        Item item = stack.getItem();

        if (item instanceof ItemStackUpgrade upgrade) {
            return wrapper.canAddStackUpgrade(upgrade.multiplier(stack));
        }

        return item instanceof ItemUpgrade<?>;
    }

}
