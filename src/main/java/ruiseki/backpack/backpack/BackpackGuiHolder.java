package ruiseki.backpack.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.storagecore.client.gui.StorageGuiHolder;
import ruiseki.storagecore.client.gui.container.StorageScreen;

public class BackpackGuiHolder extends StorageGuiHolder<BackpackPanel> {

    protected final IBackpackWrapper wrapper;

    public BackpackGuiHolder(IBackpackWrapper wrapper) {
        super(wrapper);
        this.wrapper = wrapper;
    }

    @Override
    protected BackpackPanel constructPanel(PanelSyncManager syncManager, UISettings settings, EntityPlayer player,
        TileEntity tile, int width) {
        return new BackpackPanel(player, tile, syncManager, settings, wrapper, width);
    }

    public static final class ItemStackGuiHolder extends BackpackGuiHolder
        implements IGuiHolder<PlayerInventoryGuiData> {

        public ItemStackGuiHolder(IBackpackWrapper wrapper) {
            super(wrapper);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
            return new StorageScreen(mainPanel);
        }

        @Override
        public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
            BackpackPanel panel = buildUI(syncManager, settings, data.getPlayer(), null);
            addCommonWidgets(panel);
            panel.modifyPlayerSlot(syncManager, data.getInventoryType(), data.getSlotIndex(), data.getPlayer());
            return panel;
        }
    }
}
