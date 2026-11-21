package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import ruiseki.backpack.backpack.TileBackpack;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.PlayerHelpers;

public class StorageJukeboxPlaybackAnchors {

    private static final int PLAYER_ANCHOR_GRACE_TICKS = 20;
    private static final Map<UUID, Anchor> ANCHORS = new HashMap<>();

    private StorageJukeboxPlaybackAnchors() {}

    public static Optional<JukeboxPlaybackLocation> getPlaybackLocation(WorldServer initiatingLevel, UUID groupId) {
        Anchor anchor = ANCHORS.get(groupId);
        if (anchor == null) {
            return Optional.empty();
        }

        Optional<JukeboxPlaybackLocation> location = anchor.resolve(initiatingLevel.func_73046_m(), groupId);
        if (location.isEmpty()) {
            ANCHORS.remove(groupId, anchor);
        }
        return location;
    }

    public static void clear() {
        ANCHORS.clear();
    }

    private sealed interface Anchor permits BlockAnchor,PlayerAnchor {

        Optional<JukeboxPlaybackLocation> resolve(MinecraftServer server, UUID groupId);
    }

    private record BlockAnchor(int dimension, BlockPos pos, UUID endpointId) implements Anchor {

        @Override
        public Optional<JukeboxPlaybackLocation> resolve(MinecraftServer server, UUID groupId) {
            WorldServer level = DimensionManager.getWorld(this.dimension);
            if (level == null || !pos.isLoaded(level) || !(pos.getTileEntity(level) instanceof TileBackpack))
                return Optional.empty();
            return Optional.of(JukeboxPlaybackLocation.forBlock(level, pos));
        }
    }

    private record PlayerAnchor(UUID playerId, int lastSeenTick) implements Anchor {

        @Override
        public Optional<JukeboxPlaybackLocation> resolve(MinecraftServer server, UUID groupId) {
            if (lastSeenTick < server.getTickCounter() - PLAYER_ANCHOR_GRACE_TICKS) {
                return Optional.empty();
            }
            EntityPlayerMP player = PlayerHelpers.getPlayerByUUID(server, this.playerId);
            return player == null ? Optional.empty() : Optional.of(JukeboxPlaybackLocation.forEntity(player));
        }
    }
}
