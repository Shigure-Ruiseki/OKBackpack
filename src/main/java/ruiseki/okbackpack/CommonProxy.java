package ruiseki.okbackpack;

import ruiseki.okbackpack.common.event.BackpackEventHandler;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.common.network.PacketJukeboxPlaybackState;
import ruiseki.okbackpack.common.network.PacketJukeboxPositionUpdate;
import ruiseki.okbackpack.common.network.PacketQuickDraw;
import ruiseki.okbackpack.common.network.PacketRecordDuration;
import ruiseki.okbackpack.common.network.PacketSyncCarriedItem;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.network.PacketHandler;
import ruiseki.okcore.proxy.CommonProxyComponent;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKBackpack.instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);
        packetHandler.register(PacketBackpackNBT.class);
        packetHandler.register(PacketSyncCarriedItem.class);
        packetHandler.register(PacketQuickDraw.class);
        packetHandler.register(PacketJukeboxPlaybackState.class);
        packetHandler.register(PacketJukeboxPositionUpdate.class);
        packetHandler.register(PacketRecordDuration.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        BackpackEventHandler handler = new BackpackEventHandler();
    }
}
