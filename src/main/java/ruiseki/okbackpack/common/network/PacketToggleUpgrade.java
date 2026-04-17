package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.inventory.InventoryType;

import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
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

        BackpackEntityHelper.BackpackContext context = BackpackEntityHelper.getBackpack(player, type, backpackSlot);
        if (context == null) return;

        if (upgradeSlot < 0 || upgradeSlot >= context.wrapper().upgradeSlots) return;

        IUpgradeWrapper upgradeWrapper = context.wrapper()
            .getUpgradeHandler()
            .getWrapperInSlot(upgradeSlot);
        if (upgradeWrapper instanceof IToggleable toggleable) {
            toggleable.toggle();
            BackpackEntityHelper.persistBackpack(context);
        }
    }
}
