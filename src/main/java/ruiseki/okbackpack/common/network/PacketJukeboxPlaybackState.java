package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.client.audio.BackpackJukeboxSoundManager;
import ruiseki.okbackpack.client.audio.RecordDurationClientHelper;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketJukeboxPlaybackState extends PacketCodec {

    @CodecField
    private String backpackUuid;
    @CodecField
    private int upgradeSlot;
    @CodecField
    private boolean playing;
    @CodecField
    private int currentSlotIndex;
    @CodecField
    private int progressTicks;
    @CodecField
    private float sourceX;
    @CodecField
    private float sourceY;
    @CodecField
    private float sourceZ;
    @CodecField
    private String recordName;
    @CodecField
    private int carrierEntityId;

    public PacketJukeboxPlaybackState() {}

    public PacketJukeboxPlaybackState(String backpackUuid, int upgradeSlot, boolean playing, int currentSlotIndex,
        int progressTicks, float sourceX, float sourceY, float sourceZ, String recordName, int carrierEntityId) {
        this.backpackUuid = backpackUuid;
        this.upgradeSlot = upgradeSlot;
        this.playing = playing;
        this.currentSlotIndex = currentSlotIndex;
        this.progressTicks = progressTicks;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceZ = sourceZ;
        this.recordName = recordName != null ? recordName : "";
        this.carrierEntityId = carrierEntityId;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {
        BackpackJukeboxSoundManager manager = BackpackJukeboxSoundManager.getInstance();
        if (!playing) {
            manager.stop(backpackUuid, upgradeSlot);
            return;
        }

        if (recordName.isEmpty()) {
            manager.stop(backpackUuid, upgradeSlot);
            return;
        }

        if (progressTicks == 0) {
            manager.stop(backpackUuid, upgradeSlot);
            manager.playRecord(backpackUuid, upgradeSlot, recordName, sourceX, sourceY, sourceZ, carrierEntityId);
            // Resolve real duration on client and sync back to server
            RecordDurationClientHelper.resolveAndSyncDuration(recordName);
        } else {
            manager.updatePosition(backpackUuid, upgradeSlot, sourceX, sourceY, sourceZ);
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {}
}
