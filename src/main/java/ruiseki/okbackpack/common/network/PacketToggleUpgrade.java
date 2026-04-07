package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketToggleUpgrade extends PacketCodec {

    @CodecField
    public int backpackSlot;
    @CodecField
    public int upgradeSlot;
    @CodecField
    public String typeId;

    public PacketToggleUpgrade() {}

    public PacketToggleUpgrade(int backpackSlot, int upgradeSlot, InventoryType type) {
        this.backpackSlot = backpackSlot;
        this.upgradeSlot = upgradeSlot;
        this.typeId = type.getId();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {}

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        InventoryType type = InventoryType.getFromId(typeId);
        if (type == null) return;

        IInventory inventory;
        if (type == InventoryTypes.BAUBLES) {
            inventory = BaublesApi.getBaubles(player);
        } else {
            inventory = player.inventory;
        }

        if (backpackSlot < 0 || backpackSlot >= inventory.getSizeInventory()) return;

        ItemStack stack = inventory.getStackInSlot(backpackSlot);
        if (stack == null || !(stack.getItem() instanceof BlockBackpack.ItemBackpack backpackItem)) return;

        BackpackWrapper wrapper = new BackpackWrapper(stack, backpackItem);
        wrapper.readFromItem();

        if (upgradeSlot < 0 || upgradeSlot >= wrapper.upgradeSlots) return;

        IUpgradeWrapper upgradeWrapper = wrapper.getUpgradeHandler()
            .getWrapperInSlot(upgradeSlot);
        if (upgradeWrapper instanceof IToggleable toggleable) {
            toggleable.toggle();
            wrapper.writeToItem();
        }
    }
}
