package ruiseki.okbackpack.client.gui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.widget.SelectBackpackPanel;

public class SelectBackpackGuiHolder implements IGuiHolder<GuiData> {

    public static final SimpleGuiFactory SELECT_BACKPACK = GuiFactories
        .createSimple("okbackpack:select_backpack", new SelectBackpackGuiHolder());

    private SelectBackpackGuiHolder() {}

    @Override
    public ModularScreen createScreen(GuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Reference.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        SelectBackpackPanel panel = new SelectBackpackPanel(data.getPlayer(), syncManager, settings);
        panel.addContent();
        return panel;
    }
}
