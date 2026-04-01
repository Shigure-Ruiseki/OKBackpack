package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.client.gui.handler.InventoryCraftingWrapper;
import ruiseki.okbackpack.common.block.BackpackWrapper;

public class IndexedModularCraftingSlot extends ModularCraftingSlot {

    protected InventoryCraftingWrapper craftMatrix;
    protected final BackpackWrapper wrapper;
    protected final int upgradeSlotIndex;

    public IndexedModularCraftingSlot(int upgradeSlotIndex, BackpackWrapper wrapper, IItemHandler inv, int invIndex) {
        super(inv, invIndex);
        this.wrapper = wrapper;
        this.upgradeSlotIndex = upgradeSlotIndex;
        canPut(false);
        canDragInto(false);
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        if (stack != null && stack.getItem() != null) {
            FMLCommonHandler.instance()
                .firePlayerCraftingEvent(player, stack, craftMatrix);
            onCrafting(stack);
        }

        ICraftingUpgrade upgrade = wrapper != null ? wrapper.gatherCapabilityUpgrades(ICraftingUpgrade.class)
            .get(upgradeSlotIndex) : null;

        for (int i = 0; i < craftMatrix.getSizeInventory() - 1; i++) {
            ItemStack slotStack = craftMatrix.getStackInSlot(i);
            if (slotStack == null) continue;

            ItemStack original = slotStack.copy();
            boolean extractedFromHandler = false;

            if (upgrade != null && upgrade.isUseBackpack()) {
                ItemStack extracted = wrapper.extractItem(slotStack, 1, false);
                if (extracted != null) {
                    extractedFromHandler = true;
                }
            }

            if (!extractedFromHandler) {
                slotStack.stackSize--;
                if (slotStack.stackSize <= 0) slotStack = null;
                craftMatrix.setInventorySlotContents(i, slotStack);
            }

            if (original.getItem()
                .hasContainerItem(original)) {
                ItemStack cont = original.getItem()
                    .getContainerItem(original);
                if (cont != null && cont.isItemStackDamageable() && cont.getItemDamage() > cont.getMaxDamage()) {
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, cont));
                } else if (cont != null) {
                    if (craftMatrix.getStackInSlot(i) == null) {
                        craftMatrix.setInventorySlotContents(i, cont);
                    } else if (!player.inventory.addItemStackToInventory(cont)) {
                        player.dropPlayerItemWithRandomChoice(cont, false);
                    }
                }
            }
        }

        craftMatrix.notifyContainer();
    }

    public void setCraftMatrix(InventoryCraftingWrapper craftMatrix) {
        this.craftMatrix = craftMatrix;
    }

    public int getUpgradeSlotIndex() {
        return upgradeSlotIndex;
    }
}
