package ruiseki.backpack.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.client.gui.container.BackpackContainer;
import ruiseki.backpack.client.gui.container.BackpackGuiContainer;
import ruiseki.backpack.client.gui.sync.BackpackSH;
import ruiseki.okbackpack.client.gui.slot.LockedPlayerSlot;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class BackpackPanel extends StoragePanel {

    public BackpackPanel(EntityPlayer player, TileEntity tile, PanelSyncManager syncManager, UISettings settings,
        IBackpackWrapper wrapper, int width) {
        super(player, tile, syncManager, settings, wrapper, width);
    }

    @Override
    public void constructSettings() {
        this.getSettings()
            .customContainer(() -> new BackpackContainer(this.getWrapper()));
        this.getSettings()
            .customGui(() -> (container, screen) -> new BackpackGuiContainer(container, screen, this.getWrapper()));
    }

    @Override
    protected void constructStorageHandler() {
        this.storageSH = new BackpackSH(this.getPlayerInventory(), this.getWrapper(), this);
    }

    public void modifyPlayerSlot(PanelSyncManager syncManager, InventoryType inventoryType, int slotIndex,
        EntityPlayer player) {
        if (inventoryType == InventoryTypes.BAUBLES) return;
        // The handler must stay a ModularUI player wrapper, otherwise integrations such as
        // Inventory Bogo Sorter cannot recognise these as player inventory slots.
        ModularSlot slot = new LockedPlayerSlot(player.inventory, slotIndex).slotGroup("player_inventory");
        syncManager.itemSlot("player", slotIndex, slot);
    }

    @Override
    public BackpackSH getStorageSH() {
        return (BackpackSH) this.storageSH;
    }

    @Override
    public IBackpackWrapper getWrapper() {
        return (IBackpackWrapper) super.getWrapper();
    }

    @Override
    public @NotNull BackpackPanel getPanel() {
        return (BackpackPanel) super.getPanel();
    }
}
