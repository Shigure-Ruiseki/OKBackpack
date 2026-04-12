package ruiseki.okbackpack.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

public interface IStoragePanel<T extends ModularPanel> extends IWidget {

    EntityPlayer getPlayer();

    TileEntity getTile();

    PanelSyncManager getSyncManager();

    UISettings getSettings();

    IStorageWrapper getWrapper();

    IPanelHandler getSettingPanel();

    boolean isMemorySettingTabOpened();

    void setMemorySettingTabOpened(boolean opened);

    boolean shouldMemorizeRespectNBT();

    void setShouldMemorizeRespectNBT(boolean enabled);

    boolean isSortingSettingTabOpened();

    void setSortingSettingTabOpened(boolean opened);

    IStorageContainer<?> getContainer();

    @NotNull
    T getStoragePanel();

    SyncHandler getStorageSH();

    ItemSlotSH[] getStorageSlotSH();

    ItemSlotSH[] getUpgradedSlotSH();

}
