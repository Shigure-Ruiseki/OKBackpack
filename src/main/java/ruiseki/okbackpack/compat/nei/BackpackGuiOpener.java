package ruiseki.okbackpack.compat.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerInputHandler;

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
        // if (keyCode != keyBinding.getKeyCode()) return false;
        //
        // if (!Keyboard.getEventKeyState()) return false;
        //
        // Slot slot = GuiContainerManager.getSlotMouseOver(gui);
        // if (slot == null || !slot.getHasStack()) return false;
        //
        // EntityPlayer player = Platform.getClientPlayer();
        // if (slot.inventory != player.inventory) return false;
        //
        // ItemStack stack = slot.getStack();
        // if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack)) return false;
        //
        // GuiFactories.playerInventory()
        // .openFromPlayerInventoryClient(slot.getSlotIndex());

        return false;
    }

    @Override
    public boolean mouseClicked(GuiContainer gui, int mousex, int mousey, int button) {
        // if (button != 0 && button != 1) return false;
        // if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) return false;
        //
        // Slot slot = GuiContainerManager.getSlotMouseOver(gui);
        // if (slot == null || !slot.getHasStack()) return false;
        //
        // EntityPlayer player = Platform.getClientPlayer();
        // if (slot.inventory != player.inventory) return false;
        //
        // ItemStack carried = player.inventory.getItemStack();
        // if (carried == null) return false;
        //
        // ItemStack stack = slot.getStack();
        // if (!(stack.getItem() instanceof BlockBackpack.ItemBackpack)) return false;
        //
        // BackpackHandler handler = new BackpackHandler(stack, null);
        // ItemStack remain = handler.insertItem(carried, false);
        // if (remain == null || remain.stackSize <= 0) {
        // player.inventory.setItemStack(null);
        // } else {
        // player.inventory.setItemStack(remain);
        // }
        // OmoshiroiKamo.instance.getPacketHandler()
        // .sendToServer(new PacketBackpackNBT(slot.getSlotIndex(), handler.getTagCompound(), InventoryTypes.PLAYER));
        // OmoshiroiKamo.instance.getPacketHandler()
        // .sendToServer(new PacketSyncCarriedItem(player.inventory.getItemStack()));
        return false;
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
