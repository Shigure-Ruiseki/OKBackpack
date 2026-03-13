package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.utils.item.IItemHandler;

import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedFeedingUpgradeWrapper extends AdvancedUpgradeWrapper implements IFeedingUpgrade {

    private static final String HUNGER_FEEDING_STRATEGY_TAG = "HungerFeedingStrategy";
    private static final String HURT_FEEDING_STRATEGY_TAG = "HurtFeedingStrategy";

    public AdvancedFeedingUpgradeWrapper(ItemStack upgrade) {
        super(upgrade);
        handler = new UpgradeItemStackHandler(16) {

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
    public boolean checkFilter(ItemStack stack) {
        return stack.getItem() instanceof ItemFood && super.checkFilter(stack);
    }

    @Override
    public ItemStack getFeedingStack(IItemHandler handler, int foodLevel, float health, float maxHealth) {
        int size = handler.getSlots();

        for (int i = 0; i < size; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) {
                continue;
            }

            if (!checkFilter(stack)) {
                continue;
            }

            ItemFood item = stack.getItem() instanceof ItemFood ? (ItemFood) stack.getItem() : null;
            if (item == null) {
                continue;
            }

            int healingAmount = item.func_150905_g(stack);

            if (maxHealth > health && getHealthFeedingStrategy() == FeedingStrategy.HEALTH.ALWAYS) {
                return handler.extractItem(i, 1, false);
            }

            boolean flag = false;
            switch (getHungerFeedingStrategy()) {
                case FULL:
                    flag = healingAmount <= 20 - foodLevel;
                    break;
                case HALF:
                    flag = healingAmount / 2 <= 20 - foodLevel;
                    break;
                case ALWAYS:
                    flag = foodLevel < 20;
                    break;
            }

            if (flag) {
                return handler.extractItem(i, 1, false);
            }
        }

        return null;
    }

    public FeedingStrategy.Hunger getHungerFeedingStrategy() {
        int ord = ItemNBTHelpers.getInt(upgrade, HUNGER_FEEDING_STRATEGY_TAG, FeedingStrategy.Hunger.FULL.ordinal());
        FeedingStrategy.Hunger[] vals = FeedingStrategy.Hunger.values();
        return (ord < 0 || ord >= vals.length) ? FeedingStrategy.Hunger.FULL : vals[ord];
    }

    public void setHungerFeedingStrategy(FeedingStrategy.Hunger strategy) {
        if (strategy == null) {
            strategy = FeedingStrategy.Hunger.FULL;
        }
        ItemNBTHelpers.setInt(upgrade, HUNGER_FEEDING_STRATEGY_TAG, strategy.ordinal());
    }

    public FeedingStrategy.HEALTH getHealthFeedingStrategy() {
        int ord = ItemNBTHelpers.getInt(upgrade, HURT_FEEDING_STRATEGY_TAG, FeedingStrategy.HEALTH.ALWAYS.ordinal());
        FeedingStrategy.HEALTH[] vals = FeedingStrategy.HEALTH.values();
        return (ord < 0 || ord >= vals.length) ? FeedingStrategy.HEALTH.ALWAYS : vals[ord];
    }

    public void setHealthFeedingStrategy(FeedingStrategy.HEALTH strategy) {
        if (strategy == null) {
            strategy = FeedingStrategy.HEALTH.ALWAYS;
        }
        ItemNBTHelpers.setInt(upgrade, HURT_FEEDING_STRATEGY_TAG, strategy.ordinal());
    }

    public static class FeedingStrategy {

        public enum Hunger {
            FULL,
            HALF,
            ALWAYS;
        }

        public enum HEALTH {
            ALWAYS,
            IGNORE;
        }
    }
}
