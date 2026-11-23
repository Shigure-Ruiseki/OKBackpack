package ruiseki.okstoragecore.client.gui.container;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.Reference;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class StorageScreen extends ModularScreen {

    public StorageScreen(@NotNull ModularPanel mainPanel) {
        super(Reference.MOD_ID, mainPanel);
    }

    @Override
    public void onOpen() {
        GuiScreen inventoryParent = resolveInventoryParent();
        boolean reopenInventoryParent = GeneralConfig.enableInventoryBackpackCloseReturnsToInventory
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
        if (!(getMainPanel() instanceof StoragePanel panel)) return false;
        return panel.getType() == InventoryTypes.PLAYER;
    }
}
