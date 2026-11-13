package ruiseki.backpack.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.backpack.api.IBlockPickResponseUpgrade;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.helpers.PlayerInventoryProvider;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketBlockPick extends PacketCodec {

    @CodecField
    public ItemStack filter;

    public PacketBlockPick() {}

    public PacketBlockPick(ItemStack filter) {
        this.filter = filter;
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
        if (player == null) return;
        PlayerInventoryProvider.get()
            .runOnBackpacks(
                player,
                (backpack, inventoryHandlerName, identifier, slot) -> CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(wrapper -> {
                        for (IBlockPickResponseUpgrade upgrade : wrapper.getUpgradeHandler()
                            .getWrappersThatImplement(IBlockPickResponseUpgrade.class)) {
                            if (upgrade.pickBlock(player, this.filter)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false));

    }
}
