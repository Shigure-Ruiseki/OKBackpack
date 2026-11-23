package ruiseki.okbackpack.backpack.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.backpack.ItemBackpack;
import ruiseki.okbackpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.okbackpack.upgrades.inception.ItemInceptionUpgrade;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.InventoryHandler;

public class BackpackInventoryHandler extends InventoryHandler {

    public BackpackInventoryHandler(int numberOfInventorySlots, IStorageWrapper storageWrapper,
        NBTTagCompound contentsNbt, Runnable saveHandler, int slotLimit) {
        super(numberOfInventorySlots, storageWrapper, contentsNbt, saveHandler, slotLimit);
    }

    @Override
    protected boolean isAllowed(ItemStack stack) {
        return ItemHelpers.isEmpty(stack) || (!(ItemHelpers.getItem(stack) instanceof ItemBackpack)
            || (hasInceptionUpgrade() && isBackpackWithoutInceptionUpgrade(stack)));
    }

    private boolean hasInceptionUpgrade() {
        return storageWrapper.getUpgradeHandler()
            .hasUpgrade(ItemInceptionUpgrade.TYPE);
    }

    private boolean isBackpackWithoutInceptionUpgrade(ItemStack stack) {
        return ItemHelpers.isEmpty(stack) || (ItemHelpers.getItem(stack) instanceof ItemBackpack)
            && !CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
                .map(
                    w -> w.getUpgradeHandler()
                        .hasUpgrade(ItemInceptionUpgrade.TYPE))
                .orElse(false);
    }
}
