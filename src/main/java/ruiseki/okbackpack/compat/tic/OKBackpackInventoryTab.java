package ruiseki.okbackpack.compat.tic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;

import baubles.api.BaublesApi;
import org.apache.logging.log4j.Level;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.client.gui.interaction.BackpackGuiOpenHelpers;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.common.init.ModBlocks;
import tconstruct.client.tabs.AbstractTab;

import java.lang.reflect.Field;

public class OKBackpackInventoryTab extends AbstractTab {

    private EntityClientPlayerMP cachedPlayer;
    private int cachedTick = -1;
    private TargetBackpack cachedTarget;

    public OKBackpackInventoryTab() {
        super(0, 0, 0, ModBlocks.BACKPACK_BASE.newItemStack());
    }

    @Override
    public void onTabClicked() {
        TargetBackpack target = getTargetBackpack(true);
        if (target != null) {
            BackpackGuiOpenHelpers.openClient(target.inventoryType(), target.slotIndex());
        }
    }

    @Override
    public boolean shouldAddToList() {
        return getTargetBackpack() != null;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        TargetBackpack target = getTargetBackpack();
        if (target != null) {
            drawButton(minecraft, target.stack(), true);
        }
    }

    private TargetBackpack getTargetBackpack() {
        return getTargetBackpack(false);
    }

    private TargetBackpack getTargetBackpack(boolean refresh) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player == null || player.capabilities.isCreativeMode) {
            clearTargetCache();
            return null;
        }

        if (refresh || cachedPlayer != player || cachedTick != player.ticksExisted) {
            cachedPlayer = player;
            cachedTick = player.ticksExisted;
            cachedTarget = findTargetBackpack(player);
        }
        return cachedTarget;
    }

    private TargetBackpack findTargetBackpack(EntityClientPlayerMP player) {
        TargetBackpack baublesTarget = findTargetBackpack(BaublesApi.getBaubles(player), InventoryTypes.BAUBLES);
        if (baublesTarget != null) {
            return baublesTarget;
        }
        return findTargetBackpack(player.inventory, InventoryTypes.PLAYER);
    }

    private TargetBackpack findTargetBackpack(IInventory inventory, InventoryType inventoryType) {
        if (inventory == null) {
            return null;
        }

        for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++) {
            ItemStack stack = inventory.getStackInSlot(slotIndex);
            if (BackpackEntityHelpers.isBackpackStack(stack, false)) {
                return new TargetBackpack(stack, inventoryType, slotIndex);
            }
        }
        return null;
    }

    private void clearTargetCache() {
        cachedPlayer = null;
        cachedTick = -1;
        cachedTarget = null;
    }

    @Desugar
    private record TargetBackpack(ItemStack stack, InventoryType inventoryType, int slotIndex) {}
}
