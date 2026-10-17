package ruiseki.storagecore.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.container.StorageScreen;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class StorageGuiHolder<T extends StoragePanel> {

    private final IStorageWrapper wrapper;

    public StorageGuiHolder(IStorageWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public T buildUI(PanelSyncManager syncManager, UISettings settings, EntityPlayer player, TileEntity tile) {
        int width = 20 + this.getWrapper()
            .getNumberOfSlotColumns() * ItemSlot.SIZE;
        return constructPanel(syncManager, settings, player, tile, width);
    }

    @SuppressWarnings("unchecked")
    protected T constructPanel(PanelSyncManager syncManager, UISettings settings, EntityPlayer player, TileEntity tile,
        int width) {
        return (T) new StoragePanel(player, tile, syncManager, settings, wrapper, width);
    }

    protected void addCommonWidgets(StoragePanel panel) {
        panel.addDisplayName();
        panel.addMainLayout();
        panel.addUpgradeSlots();
        panel.addUpgradeTabs();
        panel.addSettingPanel();
    }

    public IStorageWrapper getWrapper() {
        return wrapper;
    }

    public static final class TileEntityGuiHolder<T extends StoragePanel> extends StorageGuiHolder<T>
        implements IGuiHolder<PosGuiData> {

        public TileEntityGuiHolder(IStorageWrapper wrapper) {
            super(wrapper);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
            return new StorageScreen(mainPanel);
        }

        @Override
        public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
            StoragePanel panel = buildUI(syncManager, settings, data.getPlayer(), data.getTileEntity());
            addCommonWidgets(panel);
            return panel;
        }
    }
}
