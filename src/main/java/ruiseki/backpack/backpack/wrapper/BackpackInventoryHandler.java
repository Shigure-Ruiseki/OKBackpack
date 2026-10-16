package ruiseki.backpack.backpack.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.backpack.Capabilities;
import ruiseki.backpack.backpack.ItemBackpack;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.inventory.InventoryHandler;
import ruiseki.storagecore.upgrades.inception.ItemInceptionUpgrade;

public class BackpackInventoryHandler extends InventoryHandler {

    public BackpackInventoryHandler(int numberOfInventorySlots, IStorageWrapper storageWrapper,
        NBTTagCompound contentsNbt, Runnable saveHandler, int slotLimit) {
        super(numberOfInventorySlots, storageWrapper, contentsNbt, saveHandler, slotLimit);
    }

    @Override
    protected boolean isAllowed(ItemStack stack) {
        return (!(stack.getItem() instanceof ItemBackpack)
            || (hasInceptionUpgrade() && isBackpackWithoutInceptionUpgrade(stack)));
    }

    private boolean hasInceptionUpgrade() {
        return storageWrapper.getUpgradeHandler()
            .hasUpgrade(ItemInceptionUpgrade.TYPE);
    }

    private boolean isBackpackWithoutInceptionUpgrade(ItemStack stack) {
        return (stack.getItem() instanceof ItemBackpack)
            && !CapabilityHelpers.getCapability(stack, Capabilities.STORAGE_WRAPPER)
                .map(
                    w -> w.getUpgradeHandler()
                        .hasUpgrade(ItemInceptionUpgrade.TYPE))
                .orElse(false);
    }
}
