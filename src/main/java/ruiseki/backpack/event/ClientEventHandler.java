package ruiseki.backpack.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;

import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;
import com.gtnewhorizon.gtnhlib.event.PickBlockEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.helpers.InteractionHelpers;
import ruiseki.backpack.helpers.InteractionHelpers.InteractionResult;
import ruiseki.backpack.helpers.InteractionRenderHelpers;
import ruiseki.backpack.network.packet.PacketBlockPick;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.event.gui.SlotRenderEvent;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.GuiHelpers;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.client.gui.tooltip.StorageContentHandler;

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
        OKBackpack._instance.getPacketHandler()
            .sendToServer(new PacketBlockPick(result));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.fontRenderer == null) return;

        ItemStack cursorStack = mc.thePlayer.inventory.getItemStack();
        if (ItemHelpers.isEmpty(cursorStack)) return;

        InteractionRenderHelpers.renderOverlay(
            mc.fontRenderer,
            mc.thePlayer,
            cursorStack,
            event.getSlot(),
            event.getSlotX(),
            event.getSlotY());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiContainer gui)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.fontRenderer == null) return;

        ItemStack cursor = mc.thePlayer.inventory.getItemStack();
        if (ItemHelpers.isEmpty(cursor)) return;

        Slot slot = ((GuiContainerAccessor) gui).getHoveredSlot();
        if (slot == null || !slot.getHasStack()) return;

        InteractionResult result = InteractionHelpers.getResult(mc.thePlayer, slot, cursor);
        if (!result.canExecuteTransfer()) return;

        ItemStack backpack = result.backpackStack;
        if (ItemHelpers.isEmpty(backpack)) return;

        CapabilityHelpers.getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .ifPresent(
                wrapper -> GuiHelpers.drawHoveringText(
                    backpack,
                    StorageContentHandler.buildInteractionTooltipLines(wrapper),
                    event.mouseX,
                    event.mouseY,
                    gui.width,
                    gui.height,
                    -1,
                    mc.fontRenderer));
    }

}
