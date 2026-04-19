package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.inventory.InventoryType;

import ruiseki.okbackpack.client.gui.container.BackPackContainer;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketBackpackNBT extends PacketCodec {

    @CodecField
    private int slot;
    @CodecField
    private NBTTagCompound nbt;
    @CodecField
    private String typeId;

    public PacketBackpackNBT() {}

    public PacketBackpackNBT(int slot, NBTTagCompound nbt, InventoryType type) {
        this.slot = slot;
        this.nbt = nbt;
        this.typeId = type.getId();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        InventoryType type = InventoryType.getFromId(typeId);
        if (type == null || nbt == null) return;

        // Try to find the backpack by UUID in client inventory
        NBTTagCompound bp = nbt.getCompoundTag(BackpackWrapper.BACKPACK_NBT);
        if (bp == null) return;
        String uuid = bp.getString(BackpackWrapper.UUID_TAG);
        if (uuid == null || uuid.isEmpty()) return;

        ItemStack stack = BackpackEntityHelpers.findBackpackByUuid(player, uuid, type);
        if (stack != null) {
            stack.setTagCompound(nbt);
            refreshOpenBackpack(player, stack, uuid);
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        InventoryType type = InventoryType.getFromId(typeId);
        if (type == null || nbt == null) return;

        // Use UUID tracking to find the correct backpack (not slot index!)
        NBTTagCompound bp = nbt.getCompoundTag(BackpackWrapper.BACKPACK_NBT);
        if (bp == null) return;
        String uuid = bp.getString(BackpackWrapper.UUID_TAG);
        if (uuid == null || uuid.isEmpty()) return;

        ItemStack stack = BackpackEntityHelpers.findBackpackByUuid(player, uuid, type);
        if (stack != null) {
            stack.setTagCompound(nbt);
        }
    }

    private void refreshOpenBackpack(EntityPlayer player, ItemStack stack, String uuid) {
        if (!(player.openContainer instanceof BackPackContainer container)
            || !(container.wrapper instanceof BackpackWrapper wrapper)) {
            return;
        }

        if (!BackpackEntityHelpers.isSameBackpack(stack, uuid)
            || !BackpackEntityHelpers.isSameBackpack(wrapper.getBackpack(), uuid)) {
            return;
        }

        wrapper.setBackpackStack(stack);
        wrapper.readFromItem();
    }
}
