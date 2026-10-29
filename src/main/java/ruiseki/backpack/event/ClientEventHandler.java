package ruiseki.backpack.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.event.PickBlockEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.backpack.network.packet.PacketBlockPick;
import ruiseki.okbackpack.client.gui.interaction.BackpackInventoryInteractionOverlay;
import ruiseki.okcore.OKCore;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.event.gui.SlotRenderEvent;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHelpers;

public class ClientEventHandler {

    public static final ClientEventHandler INSTANCE = new ClientEventHandler();

    public ClientEventHandler() {}

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handleBlockPick(PickBlockEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || player.capabilities.isCreativeMode) return;
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        World world = player.worldObj;
        BlockPos pos = new BlockPos(hit);
        if (pos.isAirBlock(world)) return;
        ItemStack result = pos.getBlock(world)
            .getPickBlock(hit, world, pos.getX(), pos.getY(), pos.getZ(), player);
        if (ItemHelpers.isEmpty(result)) return;
        if (InventoryHelpers.findSlotMatchingItem(player, result) >= 0) return;
        OKCore._instance.getPacketHandler()
            .sendToServer(new PacketBlockPick(result));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.fontRenderer == null) return;

        Slot slot = event.getSlot();
        if (slot == null) return;

        ItemStack cursorStack = mc.thePlayer.inventory.getItemStack();
        if (cursorStack == null || cursorStack.stackSize <= 0) return;

        BackpackInventoryInteractionOverlay.renderOverlay(mc.fontRenderer, mc.thePlayer, slot, cursorStack);
    }
}
