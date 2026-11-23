package ruiseki.okstoragecore.upgrades.jukebox;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;

import ruiseki.okcore.datastructure.BlockPos;

public record JukeboxPlaybackLocation(WorldServer world, @Nullable BlockPos blockPos, @Nullable EntityPlayer entity) {

    public static JukeboxPlaybackLocation forBlock(WorldServer level, BlockPos blockPos) {
        return new JukeboxPlaybackLocation(level, blockPos, null);
    }

    public static JukeboxPlaybackLocation forEntity(EntityPlayer entity) {
        return new JukeboxPlaybackLocation((WorldServer) entity.worldObj, null, entity);
    }
}
