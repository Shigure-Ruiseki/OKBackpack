package ruiseki.okstoragecore.network.packet;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okstoragecore.upgrades.jukebox.StorageSoundHandler;

public class PacketStopDiscPlayback extends PacketCodec {

    @CodecField
    private UUID storageUuid = null;

    public PacketStopDiscPlayback() {

    }

    public PacketStopDiscPlayback(UUID storageUuid) {
        this.storageUuid = storageUuid;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        StorageSoundHandler.stopStorageSound(this.storageUuid);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
