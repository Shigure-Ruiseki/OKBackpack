package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketTankClick extends PacketCodec {

    @CodecField
    public int upgradeSlot;

    public PacketTankClick() {}

    public PacketTankClick(int upgradeSlot) {
        this.upgradeSlot = upgradeSlot;
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
        if (!(openContainer instanceof BackPackContainer backpackContainer)) return;

        BackpackWrapper wrapper = (BackpackWrapper) backpackContainer.wrapper;

        if (upgradeSlot < 0 || upgradeSlot >= wrapper.getUpgradeHandler()
            .getSlots()) return;

        IUpgradeWrapper upgradeWrapper = wrapper.getUpgradeHandler()
            .getWrapperInSlot(upgradeSlot);

        if (upgradeWrapper instanceof ITankUpgrade tankUpgrade) {
            tankUpgrade.interactWithCursorStack(player);

            // Sync the cursor stack back to client
            ItemStack cursorStack = player.inventory.getItemStack();
            player.updateHeldItem();
        }
    }
}
