package ruiseki.okstoragecore.proxy;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.network.PacketHandler;
import ruiseki.okcore.proxy.CommonProxyComponent;
import ruiseki.okstoragecore.OKStorageCore;
import ruiseki.okstoragecore.network.packet.PacketPlayDisc;
import ruiseki.okstoragecore.network.packet.PacketStopDiscPlayback;
import ruiseki.okstoragecore.network.packet.PacketTankClick;
import ruiseki.okstoragecore.upgrades.jukebox.ServerStorageSoundHandler;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKStorageCore._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        packetHandler.register(PacketTankClick.class);
        packetHandler.register(PacketPlayDisc.class);
        packetHandler.register(PacketStopDiscPlayback.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(ServerStorageSoundHandler.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(ServerStorageSoundHandler.INSTANCE);
    }
}
