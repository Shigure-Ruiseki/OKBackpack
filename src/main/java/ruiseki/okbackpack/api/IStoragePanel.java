package ruiseki.okbackpack.api;

import com.cleanroommc.modularui.api.IPanelHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public interface IStoragePanel {

    EntityPlayer getPlayer();

    TileEntity getTile();

    PanelSyncManager getSyncManager();

    UISettings getSettings();

    IStorageWrapper getWrapper();

    IPanelHandler getSettingPanel();

    boolean isMemorySettingTabOpened();

    boolean shouldMemorizeRespectNBT();

    boolean isSortingSettingTabOpened();
}
