package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketQuickDraw extends PacketCodec {

    @CodecField
    public int slot;
    @CodecField
    public ItemStack stack;

    public PacketQuickDraw() {}

    public PacketQuickDraw(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        player.inventory.setInventorySlotContents(slot, stack);
    }
}
