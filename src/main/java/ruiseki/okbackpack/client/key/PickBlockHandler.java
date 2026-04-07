package ruiseki.okbackpack.client.key;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.github.bsideup.jabel.Desugar;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.wrapper.IRefillUpgrade;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.common.network.PacketQuickDraw;
import ruiseki.okcore.client.key.IKeyHandler;
import ruiseki.okcore.helper.ItemStackHelpers;

public class PickBlockHandler implements IKeyHandler {

    @Override
    public void onKeyPressed(KeyBinding keyBinding) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null || player.capabilities.isCreativeMode) return;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        Block block = mc.theWorld.getBlock(mop.blockX, mop.blockY, mop.blockZ);
        int meta = mc.theWorld.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
        Item item = Item.getItemFromBlock(block);
        if (item == null) return;

        ItemStack wanted = new ItemStack(item, 1, meta);

        boolean haveItem = false;

        // Bauble
        IInventory baublesInventory = BaublesApi.getBaubles(player);
        for (int i = 0; i < baublesInventory.getSizeInventory(); i++) {
            ItemStack stack = baublesInventory.getStackInSlot(i);

            if (stack != null && ItemStackHelpers.areStacksEqual(stack, wanted)) {
                haveItem = true;
                break;
            }
        }

        // Main inventory
        if (!haveItem) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack != null && ItemStackHelpers.areStacksEqual(stack, wanted)) {
                    haveItem = true;
                    break;
                }
            }
        }

        // Armor inventory
        if (!haveItem) {
            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack != null && ItemStackHelpers.areStacksEqual(stack, wanted)) {
                    haveItem = true;
                    break;
                }
            }
        }

        if (haveItem) return;

        ItemStack held = player.inventory.getStackInSlot(player.inventory.currentItem);

        // When hand is empty, try standard quick-draw logic
        if (held == null) {
            ItemStack result = BackpackInventoryHelpers
                .getQuickDrawStack(baublesInventory, wanted, InventoryTypes.BAUBLES);
            if (result == null) {
                result = BackpackInventoryHelpers.getQuickDrawStack(player.inventory, wanted, InventoryTypes.PLAYER);
            }
            if (result != null && mc.theWorld.isRemote) {
                int slot = player.inventory.currentItem;
                OKBackpack.instance.getPacketHandler()
                    .sendToServer(new PacketQuickDraw(slot, result));
                return;
            }
        }

        // Try advanced refill upgrade path (works with both empty and non-empty hand)
        if (!mc.theWorld.isRemote) return;

        RefillPickContext ctx = findRefillUpgrade(baublesInventory, InventoryTypes.BAUBLES);
        if (ctx == null) {
            ctx = findRefillUpgrade(player.inventory, InventoryTypes.PLAYER);
        }
        if (ctx == null) return;

        // Search this backpack for the wanted item
        ItemStack extracted = ctx.wrapper.extractItem(wanted, wanted.getMaxStackSize(), false);
        if (extracted == null) return;

        // Stash current held item into backpack if present
        if (held != null) {
            ctx.wrapper.insertItem(held, false);
        }

        // Sync backpack contents and set hand item
        int currentSlot = player.inventory.currentItem;
        OKBackpack.instance.getPacketHandler()
            .sendToServer(new PacketBackpackNBT(ctx.backpackSlot, ctx.wrapper.getBackpackNBT(), ctx.inventoryType));
        OKBackpack.instance.getPacketHandler()
            .sendToServer(new PacketQuickDraw(currentSlot, extracted));
    }

    private RefillPickContext findRefillUpgrade(IInventory inventory, InventoryType type) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack backpack)) continue;

            BackpackWrapper wrapper = new BackpackWrapper(stack, backpack);
            Map<Integer, IRefillUpgrade> upgrades = wrapper.gatherCapabilityUpgrades(IRefillUpgrade.class);

            for (IRefillUpgrade upgrade : upgrades.values()) {
                if (upgrade.supportsBlockPick() && upgrade.isEnabled()) {
                    return new RefillPickContext(wrapper, i, type);
                }
            }
        }
        return null;
    }

    @Desugar
    private record RefillPickContext(BackpackWrapper wrapper, int backpackSlot, InventoryType inventoryType) {

    }
}
