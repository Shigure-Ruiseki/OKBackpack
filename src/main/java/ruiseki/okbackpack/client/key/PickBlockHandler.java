package ruiseki.okbackpack.client.key;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import com.cleanroommc.modularui.factory.inventory.InventoryTypes;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.helpers.BackpackInventoryHelpers;
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

        ItemStack held = player.inventory.getStackInSlot(player.inventory.currentItem);
        if (held != null) return;

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

        ItemStack result;
        result = BackpackInventoryHelpers.getQuickDrawStack(baublesInventory, wanted, InventoryTypes.BAUBLES);

        if (result == null) {
            result = BackpackInventoryHelpers.getQuickDrawStack(player.inventory, wanted, InventoryTypes.PLAYER);
        }

        if (result != null && mc.theWorld.isRemote) {
            int slot = player.inventory.currentItem;
            OKBackpack.instance.getPacketHandler()
                .sendToServer(new PacketQuickDraw(slot, result));
        }
    }
}
