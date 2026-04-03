package ruiseki.okbackpack.api;

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

}
