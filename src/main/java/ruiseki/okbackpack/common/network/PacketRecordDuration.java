package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.common.item.jukebox.RecordDurationCache;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

/**
 * C→S packet: client sends resolved record duration to the server after playback starts.
 * Server caches the duration for future use since it cannot read OGG files directly.
 */
public class PacketRecordDuration extends PacketCodec {

    @CodecField
    private String recordName;
    @CodecField
    private int durationTicks;

    public PacketRecordDuration() {}

    public PacketRecordDuration(String recordName, int durationTicks) {
        this.recordName = recordName != null ? recordName : "";
        this.durationTicks = durationTicks;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {}

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if (recordName.isEmpty() || durationTicks <= 0) return;
        RecordDurationCache.putDuration(recordName, durationTicks);
    }
}
