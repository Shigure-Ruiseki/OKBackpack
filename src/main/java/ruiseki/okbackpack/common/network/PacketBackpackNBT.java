package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.inventory.InventoryType;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.compat.Mods;
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

        ItemStack stack = findStackByUUID(player, uuid);
        if (stack != null) {
            stack.setTagCompound(nbt);
        }
    }

    private ItemStack findStackByUUID(EntityPlayer player, String uuid) {
        // Check held item
        ItemStack held = player.getHeldItem();
        if (isMatch(held, uuid)) return held;

        // Check inventory
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack s = player.inventory.getStackInSlot(i);
            if (isMatch(s, uuid)) return s;
        }

        // Check Baubles
        if (Mods.Baubles.isLoaded()) {
            IInventory baubles = BaublesApi.getBaubles(player);
            if (baubles != null) {
                for (int i = 0; i < baubles.getSizeInventory(); i++) {
                    ItemStack s = baubles.getStackInSlot(i);
                    if (isMatch(s, uuid)) return s;
                }
            }
        }
        return null;
    }

    private boolean isMatch(ItemStack stack, String uuid) {
        if (stack == null || !(stack.getItem() instanceof BlockBackpack.ItemBackpack)) return false;
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound bp = tag.getCompoundTag(BackpackWrapper.BACKPACK_NBT);
        return bp != null && uuid.equals(bp.getString(BackpackWrapper.UUID_TAG));
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

        ItemStack stack = findStackByUUID(player, uuid);
        if (stack != null) {
            stack.setTagCompound(nbt);
        }
    }
}
