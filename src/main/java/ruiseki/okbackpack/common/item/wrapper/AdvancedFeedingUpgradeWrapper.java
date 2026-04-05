package ruiseki.okbackpack.common.item.wrapper;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.cleanroommc.modularui.utils.item.IItemHandler;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IBasicFilterable;
import ruiseki.okbackpack.api.wrapper.IFeedingUpgrade;
import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedFeedingUpgradeWrapper extends AdvancedUpgradeWrapper implements IFeedingUpgrade {

    private static final String HUNGER_FEEDING_STRATEGY_TAG = "HungerFeedingStrategy";
    private static final String HURT_FEEDING_STRATEGY_TAG = "HurtFeedingStrategy";

    public AdvancedFeedingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
        handler = new BaseItemStackHandler(16) {

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack != null && stack.getItem() instanceof ItemFood;
            }

            @Override
            protected void onContentsChanged(int slot) {
                NBTTagCompound tag = ItemNBTHelpers.getNBT(upgrade);
                tag.setTag(IBasicFilterable.FILTER_ITEMS_TAG, this.serializeNBT());
                storage.markDirty();
            }
        };
        NBTTagCompound handlerTag = ItemNBTHelpers.getCompound(upgrade, FILTER_ITEMS_TAG, false);
        if (handlerTag != null) handler.deserializeNBT(handlerTag);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_feeding_settings";
    }

    @Override
    public boolean checkFilter(ItemStack stack) {
        return stack.getItem() instanceof ItemFood && super.checkFilter(stack);
    }

    @Override
    public int getFoodSlot(IItemHandler handler, int foodLevel, float health, float maxHealth) {

        int missingHunger = 20 - foodLevel;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!checkFilter(stack)) continue;

            ItemFood item = stack.getItem() instanceof ItemFood ? (ItemFood) stack.getItem() : null;
            if (item == null) continue;

            int healingAmount = item.func_150905_g(stack);

            if (maxHealth > health && getHealthFeedingStrategy() == FeedingStrategy.HEALTH.ALWAYS) return slot;

            boolean flag = switch (getHungerFeedingStrategy()) {
                case FULL -> healingAmount <= missingHunger;
                case HALF -> healingAmount / 2 <= missingHunger;
                case ALWAYS -> foodLevel < 20;
                default -> false;
            };

            if (flag) return slot;
        }
        return -1;
    }

    public FeedingStrategy.Hunger getHungerFeedingStrategy() {
        int ord = ItemNBTHelpers.getInt(upgrade, HUNGER_FEEDING_STRATEGY_TAG, FeedingStrategy.Hunger.FULL.ordinal());
        FeedingStrategy.Hunger[] vals = FeedingStrategy.Hunger.values();
        return (ord < 0 || ord >= vals.length) ? FeedingStrategy.Hunger.FULL : vals[ord];
    }

    public void setHungerFeedingStrategy(FeedingStrategy.Hunger strategy) {
        if (strategy == null) strategy = FeedingStrategy.Hunger.FULL;
        ItemNBTHelpers.setInt(upgrade, HUNGER_FEEDING_STRATEGY_TAG, strategy.ordinal());
    }

    public FeedingStrategy.HEALTH getHealthFeedingStrategy() {
        int ord = ItemNBTHelpers.getInt(upgrade, HURT_FEEDING_STRATEGY_TAG, FeedingStrategy.HEALTH.ALWAYS.ordinal());
        FeedingStrategy.HEALTH[] vals = FeedingStrategy.HEALTH.values();
        return (ord < 0 || ord >= vals.length) ? FeedingStrategy.HEALTH.ALWAYS : vals[ord];
    }

    public void setHealthFeedingStrategy(FeedingStrategy.HEALTH strategy) {
        if (strategy == null) strategy = FeedingStrategy.HEALTH.ALWAYS;
        ItemNBTHelpers.setInt(upgrade, HURT_FEEDING_STRATEGY_TAG, strategy.ordinal());
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) return false;
        if (player.ticksExisted % 20 != 0) return false;
        return feed(player, storage);
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        if (world.getWorldTime() % 20 != 0) return false;

        double range = 5;

        AxisAlignedBB aabb = AxisAlignedBB
            .getBoundingBox(pos.x - range, pos.y - range, pos.z - range, pos.x + range, pos.y + range, pos.z + range);

        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
        if (players.isEmpty()) return false;

        boolean fedAny = false;

        for (EntityPlayer player : players) {
            if (player.capabilities.isCreativeMode) continue;

            if (feed(player, storage)) {
                fedAny = true;
            }
        }

        return fedAny;
    }
}
