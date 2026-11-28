package ruiseki.okbackpack.client.gui.widget;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.layout.Flow;

public class SelectBackpackPanel extends ModularPanel {

    protected final EntityPlayer player;
    protected final PanelSyncManager syncManager;
    protected final UISettings settings;

    public SelectBackpackPanel(EntityPlayer player, PanelSyncManager syncManager, UISettings settings) {
        super("gui");
        this.player = player;
        this.syncManager = syncManager;
        this.settings = settings;

        this.coverChildren();
    }

    public void addContent() {
        Flow column = Flow.column()
            .coverChildren();
        this.child(column);
    }
}
