package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.utils.item.IItemHandler;

import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class FeedingUpgradeWrapper extends BasicUpgradeWrapper implements IFeedingUpgrade {

    public FeedingUpgradeWrapper(ItemStack upgrade) {
        super(upgrade);
        handler = new UpgradeItemStackHandler(9) {

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack != null && stack.getItem() instanceof ItemFood;
            }

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(IBasicFilterable.FILTER_ITEMS_TAG, this.serializeNBT());
            }
        };
    }

    @Override
    public ItemStack getFeedingStack(IItemHandler handler, int foodLevel, float health, float maxHealth) {
        int size = handler.getSlots();
        for (int i = 0; i < size; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack == null) {
                continue;
            }
            if (!(stack.getItem() instanceof ItemFood food)) {
                continue;
            }
            int healingAmount = food.func_150905_g(stack);
            if (healingAmount <= 20 - foodLevel && checkFilter(stack)) {
                return handler.extractItem(i, 1, false);
            }
        }
        return null;
    }

    @Override
    public boolean checkFilter(ItemStack check) {
        return check.getItem() instanceof ItemFood && super.checkFilter(check);
    }

}
