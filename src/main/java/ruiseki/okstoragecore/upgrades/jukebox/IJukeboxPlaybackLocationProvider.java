package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.Optional;

import net.minecraft.world.WorldServer;

public interface IJukeboxPlaybackLocationProvider {

    Optional<JukeboxPlaybackLocation> getJukeboxPlaybackLocation(WorldServer initiatingLevel);
}
