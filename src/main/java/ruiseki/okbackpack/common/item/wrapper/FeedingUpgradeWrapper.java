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

public class FeedingUpgradeWrapper extends BasicUpgradeWrapper implements IFeedingUpgrade {

    public FeedingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
        handler = new BaseItemStackHandler(9) {

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
        return "gui.backpack.feeding_settings";
    }

    @Override
    public int getFoodSlot(IItemHandler handler, int foodLevel, float health, float maxHealth) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!checkFilter(stack)) continue;

            ItemFood item = stack.getItem() instanceof ItemFood ? (ItemFood) stack.getItem() : null;
            if (item == null) continue;

            int healingAmount = item.func_150905_g(stack);

            if (healingAmount <= 20 - foodLevel) return slot;
        }
        return -1;
    }

    @Override
    public boolean checkFilter(ItemStack check) {
        return check.getItem() instanceof ItemFood && super.checkFilter(check);
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
