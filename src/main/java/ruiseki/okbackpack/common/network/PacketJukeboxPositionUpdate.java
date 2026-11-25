package ruiseki.okbackpack.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okbackpack.client.audio.BackpackJukeboxSoundManager;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class PacketJukeboxPositionUpdate extends PacketCodec {

    @CodecField
    private String backpackUuid;
    @CodecField
    private int upgradeSlot;
    @CodecField
    private float sourceX;
    @CodecField
    private float sourceY;
    @CodecField
    private float sourceZ;

    public PacketJukeboxPositionUpdate() {}

    public PacketJukeboxPositionUpdate(String backpackUuid, int upgradeSlot, float sourceX, float sourceY,
        float sourceZ) {
        this.backpackUuid = backpackUuid;
        this.upgradeSlot = upgradeSlot;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceZ = sourceZ;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {
        BackpackJukeboxSoundManager.getInstance()
            .updatePosition(backpackUuid, upgradeSlot, sourceX, sourceY, sourceZ);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {}
}
