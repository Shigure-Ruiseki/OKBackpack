package ruiseki.okstoragecore.client.gui.container;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularScreen;

public class StorageGuiContainer extends GuiContainerWrapper {

    public StorageGuiContainer(StorageContainer container, ModularScreen screen) {
        super(container, screen);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }
}
