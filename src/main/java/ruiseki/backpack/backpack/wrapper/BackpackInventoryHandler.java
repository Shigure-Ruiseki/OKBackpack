package ruiseki.backpack.backpack.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.backpack.backpack.ItemBackpack;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.InventoryHandler;
import ruiseki.okstoragecore.upgrades.inception.ItemInceptionUpgrade;

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
            && !CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
                .map(
                    w -> w.getUpgradeHandler()
                        .hasUpgrade(ItemInceptionUpgrade.TYPE))
                .orElse(false);
    }
}
