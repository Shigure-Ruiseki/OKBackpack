package ruiseki.okstoragecore.proxy;

import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.network.PacketHandler;
import ruiseki.okcore.proxy.CommonProxyComponent;
import ruiseki.okstoragecore.OKStorageCore;
import ruiseki.okstoragecore.network.packet.PacketTankClick;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKStorageCore._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        packetHandler.register(PacketTankClick.class);
    }
}
