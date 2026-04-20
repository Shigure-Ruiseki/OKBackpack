package ruiseki.okbackpack.client.gui.container;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.config.ModConfig;

public class BackpackModularScreen extends ModularScreen {

    public BackpackModularScreen(ModularPanel mainPanel) {
        super(Reference.MOD_ID, mainPanel);
    }

    @Override
    public void onOpen() {
        GuiScreen inventoryParent = resolveInventoryParent();
        boolean reopenInventoryParent = ModConfig.enableInventoryBackpackCloseReturnsToInventory
            && inventoryParent != null
            && isPlayerInventoryBackpack();

        if (reopenInventoryParent) {
            getContext().setParentScreen(inventoryParent);
        }

        openParentOnClose(reopenInventoryParent);
        super.onOpen();
    }

    private GuiScreen resolveInventoryParent() {
        GuiScreen parent = getContext().getParentScreen();
        while (parent != null) {
            if (parent instanceof GuiInventory) {
                return parent;
            }
            if (!(parent instanceof IMuiScreen muiScreen)) {
                return null;
            }
            parent = muiScreen.getScreen()
                .getContext()
                .getParentScreen();
        }
        return null;
    }

    private boolean isPlayerInventoryBackpack() {
        if (!(getContainer() instanceof BackPackContainer container)) {
            return false;
        }
        return container.wrapper.getType() == InventoryTypes.PLAYER;
    }
}
