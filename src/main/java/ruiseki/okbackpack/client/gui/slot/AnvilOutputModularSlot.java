package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IAnvilUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okcore.item.IItemHandler;

public class AnvilOutputModularSlot extends ModularSlot {

    private final IStorageWrapper storageWrapper;
    private final int upgradeSlotIndex;

    public AnvilOutputModularSlot(IItemHandler handler, int slotIndex, IStorageWrapper storageWrapper,
        int upgradeSlotIndex) {
        super(handler, slotIndex);
        this.storageWrapper = storageWrapper;
        this.upgradeSlotIndex = upgradeSlotIndex;
    }

    @Nullable
    private IAnvilUpgrade getAnvilWrapper() {
        IUpgradeWrapper wrapper = storageWrapper.getUpgradeHandler()
            .getWrapperInSlot(upgradeSlotIndex);
        return wrapper instanceof IAnvilUpgrade ? (IAnvilUpgrade) wrapper : null;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        IAnvilUpgrade anvil = getAnvilWrapper();
        if (anvil == null) return false;
        int cost = anvil.getMaximumCost();
        if (cost <= 0 || !getHasStack()) return false;
        if (player.capabilities.isCreativeMode) return true;
        if (cost >= 40) return false;
        return player.experienceLevel >= cost;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        IAnvilUpgrade anvil = getAnvilWrapper();
        if (anvil != null) {
            anvil.onTakeOutput(player);
        }
        super.onPickupFromSlot(player, stack);
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        return false;
    }
}
