package ruiseki.okbackpack.proxy;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.event.CommonEventHandler;
import ruiseki.okbackpack.network.packet.PacketBlockPick;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.network.PacketHandler;
import ruiseki.okcore.proxy.CommonProxyComponent;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKBackpack._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);
        packetHandler.register(PacketBlockPick.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(CommonEventHandler.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(CommonEventHandler.INSTANCE);

    }
}
