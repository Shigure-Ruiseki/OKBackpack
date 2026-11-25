package ruiseki.okbackpack.common.item.jukebox;

import lombok.Getter;
import ruiseki.okbackpack.api.wrapper.IJukeboxUpgrade.JukeboxLoopMode;

@Getter
public class JukeboxPlaybackState {

    private final boolean playing;
    private final int currentSlotIndex;
    private final int progressTicks;
    private final boolean shuffleEnabled;
    private final JukeboxLoopMode loopMode;

    public JukeboxPlaybackState(boolean playing, int currentSlotIndex, int progressTicks, boolean shuffleEnabled,
        JukeboxLoopMode loopMode) {
        this.playing = playing;
        this.currentSlotIndex = currentSlotIndex;
        this.progressTicks = progressTicks;
        this.shuffleEnabled = shuffleEnabled;
        this.loopMode = loopMode;
    }
}
