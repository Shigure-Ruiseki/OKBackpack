package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketToolSwap extends PacketCodec {

    @CodecField
    public int slotA;
    @CodecField
    public int slotB;

    public PacketToolSwap() {}

    public PacketToolSwap(int slotA, int slotB) {
        this.slotA = slotA;
        this.slotB = slotB;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {}

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if (slotA < 0 || slotA >= player.inventory.mainInventory.length) return;
        if (slotB < 0 || slotB >= player.inventory.mainInventory.length) return;

        ItemStack stackA = player.inventory.getStackInSlot(slotA);
        ItemStack stackB = player.inventory.getStackInSlot(slotB);
        player.inventory.setInventorySlotContents(slotA, stackB);
        player.inventory.setInventorySlotContents(slotB, stackA);
    }
}
