package ruiseki.backpack.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okstoragecore.client.gui.StorageGuiHolder;
import ruiseki.okstoragecore.client.gui.container.StorageScreen;

public class BackpackGuiHolder extends StorageGuiHolder<BackpackPanel> {

    @Override
    public BackpackPanel buildUI(PanelSyncManager syncManager, UISettings settings, EntityPlayer player,
        TileEntity tile, Integer slotIndex, InventoryType type) {
        return new BackpackPanel(player, tile, syncManager, settings, slotIndex, type);
    }

    public static final class ItemStackGuiHolder extends BackpackGuiHolder
        implements IGuiHolder<PlayerInventoryGuiData> {

        @Override
        @SideOnly(Side.CLIENT)
        public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
            return new StorageScreen(mainPanel);
        }

        @Override
        public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
            BackpackPanel panel = buildUI(
                syncManager,
                settings,
                data.getPlayer(),
                null,
                data.getSlotIndex(),
                data.getInventoryType());
            addCommonWidgets(panel);
            panel.modifyPlayerSlot(syncManager, data.getInventoryType(), data.getSlotIndex(), data.getPlayer());
            return panel;
        }
    }
}
