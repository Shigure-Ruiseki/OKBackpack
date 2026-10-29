package ruiseki.okstoragecore.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okstoragecore.client.gui.container.StorageScreen;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class StorageGuiHolder<T extends StoragePanel> {

    @SuppressWarnings("unchecked")
    public T buildUI(PanelSyncManager syncManager, UISettings settings, EntityPlayer player, TileEntity tile,
        Integer slotIndex, InventoryType type) {
        return (T) new StoragePanel(player, tile, syncManager, settings, slotIndex, type);
    }

    protected void addCommonWidgets(StoragePanel panel) {
        panel.addDisplayName();
        panel.addMainLayout();
        panel.addUpgradeSlots();
        panel.addUpgradeTabs();
        panel.addSettingPanel();
    }

    public static final class TileEntityGuiHolder<T extends StoragePanel> extends StorageGuiHolder<T>
        implements IGuiHolder<PosGuiData> {

        @Override
        @SideOnly(Side.CLIENT)
        public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
            return new StorageScreen(mainPanel);
        }

        @Override
        public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
            StoragePanel panel = buildUI(syncManager, settings, data.getPlayer(), data.getTileEntity(), null, null);
            addCommonWidgets(panel);
            return panel;
        }
    }
}
