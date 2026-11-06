package ruiseki.okstoragecore.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;
import ruiseki.okstoragecore.upgrades.tank.TankUpgradeWrapper;

public class PacketTankClick extends PacketCodec {

    @CodecField
    public int slotIndex;

    public PacketTankClick() {}

    public PacketTankClick(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {}

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        Container openContainer = player.openContainer;
        if (!(openContainer instanceof StorageContainer container)) return;
        IStorageWrapper wrapper = container.getStorageWrapper();
        if (slotIndex < 0 || slotIndex >= wrapper.getUpgradeHandler()
            .getSlots()) return;
        IUpgradeWrapper upgradeWrapper = wrapper.getUpgradeHandler()
            .getSlotWrapper(slotIndex);
        if (upgradeWrapper instanceof TankUpgradeWrapper tankUpgrade) {
            if (tankUpgrade.transferCursorFluid(player)) {
                player.updateHeldItem();
                player.inventoryContainer.detectAndSendChanges();
            }
        }
    }
}
