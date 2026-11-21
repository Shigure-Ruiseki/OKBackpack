package ruiseki.okstoragecore.network.packet;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.world.World;

import ruiseki.okcore.audio.record.RecordData;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.RecordHelpers;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okstoragecore.upgrades.jukebox.StorageSoundHandler;

public class PacketPlayDisc extends PacketCodec {

    @CodecField
    private boolean blockStorage = false;
    @CodecField
    private UUID storageUuid = null;
    @CodecField
    private int musicDiscItemId = -1;
    @CodecField
    private int entityId = -1;
    @CodecField
    private BlockPos pos = new BlockPos(0, 0, 0);

    public PacketPlayDisc() {

    }

    public PacketPlayDisc(UUID storageUuid, int musicDiscItemId, BlockPos pos) {
        blockStorage = true;
        this.storageUuid = storageUuid;
        this.musicDiscItemId = musicDiscItemId;
        this.pos = pos;
    }

    public PacketPlayDisc(UUID storageUuid, int musicDiscItemId, int entityId) {
        blockStorage = false;
        this.storageUuid = storageUuid;
        this.musicDiscItemId = musicDiscItemId;
        this.entityId = entityId;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        Item discItem = Item.getItemById(this.musicDiscItemId);
        if (!(discItem instanceof ItemRecord record)) return;
        RecordData soundEvent = RecordHelpers.getData(player, record);
        if (this.blockStorage) {
            StorageSoundHandler.playStorageSound(soundEvent, this.storageUuid, this.pos);
        } else {
            StorageSoundHandler.playStorageSound(soundEvent, this.storageUuid, this.entityId, world, player);
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
