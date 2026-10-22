package ruiseki.okstoragecore.client.gui.container;

import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import ruiseki.okstoragecore.api.IStorageWrapper;

public class StorageGuiContainer extends GuiContainerWrapper {

    protected final IStorageWrapper wrapper;

    public StorageGuiContainer(ModularContainer container, ModularScreen screen, IStorageWrapper wrapper) {
        super(container, screen);
        this.wrapper = wrapper;
    }
}
