package ruiseki.backpack.client.gui.container;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageGuiContainer;

public class BackpackGuiContainer extends StorageGuiContainer {

    protected final IBackpackWrapper wrapper;

    public BackpackGuiContainer(ModularContainer container, ModularScreen screen, IBackpackWrapper wrapper) {
        super(container, screen, wrapper);
        this.wrapper = wrapper;
    }
}
