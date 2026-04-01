package ruiseki.okbackpack.compat.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.utils.Platform;

import baubles.common.container.InventoryBaubles;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerInputHandler;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.common.network.PacketSyncCarriedItem;

/*
 * Base on NoHotbarNeeded
 */
public class BackpackGuiOpener implements IContainerInputHandler {

    private final KeyBinding keyBinding;

    public BackpackGuiOpener(KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
        GuiContainerManager.addInputHandler(this);
    }

    @Override
    public boolean keyTyped(GuiContainer gui, char keyChar, int keyCode) {
        return false;
    }

    @Override
    public void onKeyTyped(GuiContainer gui, char keyChar, int keyID) {

    }

    @Override
    public boolean lastKeyTyped(GuiContainer gui, char keyChar, int keyCode) {
        if (keyCode != keyBinding.getKeyCode()) return false;

        if (!Keyboard.getEventKeyState()) return false;

        Slot slot = GuiContainerManager.getSlotMouseOver(gui);
        if (slot == null || !slot.getHasStack()) return false;

        EntityPlayer player = Platform.getClientPlayer();
        if (player.capabilities.isCreativeMode) return false;

        ItemStack stack = slot.getStack();
        if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack)) return false;
        int slotIndex = slot.getSlotIndex();

        if (slot.inventory instanceof InventoryPlayer) {
            GuiFactories.playerInventory()
                .openFromPlayerInventoryClient(slotIndex);
            return true;
        }

        if (slot.inventory instanceof InventoryBaubles) {
            GuiFactories.playerInventory()
                .openFromBaublesClient(slotIndex);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button) {
        if (button != 0 && button != 1) return false;
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) return false;

        Slot slot = GuiContainerManager.getSlotMouseOver(gui);
        if (slot == null || !slot.getHasStack()) return false;

        EntityPlayer player = Platform.getClientPlayer();
        if (player.capabilities.isCreativeMode) return false;
        if (!(slot.inventory instanceof InventoryPlayer)) return false;

        ItemStack carried = player.inventory.getItemStack();
        if (carried == null) return false;

        ItemStack stack = slot.getStack();
        if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack)) return false;

        BackpackWrapper wrapper = new BackpackWrapper(stack);
        ItemStack remain = wrapper.insertItem(carried, false);
        if (remain == null || remain.stackSize <= 0) {
            player.inventory.setItemStack(null);
        } else {
            player.inventory.setItemStack(remain);
        }
        OKBackpack.instance.getPacketHandler()
            .sendToServer(new PacketBackpackNBT(slot.getSlotIndex(), wrapper.getTagCompound(), InventoryTypes.PLAYER));
        OKBackpack.instance.getPacketHandler()
            .sendToServer(new PacketSyncCarriedItem(player.inventory.getItemStack()));
        return true;
    }

    @Override
    public void onMouseClicked(GuiContainer gui, int mousex, int mousey, int button) {

    }

    @Override
    public void onMouseUp(GuiContainer gui, int mousex, int mousey, int button) {

    }

    @Override
    public boolean mouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {
        return false;
    }

    @Override
    public void onMouseScrolled(GuiContainer gui, int mousex, int mousey, int scrolled) {

    }

    @Override
    public void onMouseDragged(GuiContainer gui, int mousex, int mousey, int button, long heldTime) {

    }
}
