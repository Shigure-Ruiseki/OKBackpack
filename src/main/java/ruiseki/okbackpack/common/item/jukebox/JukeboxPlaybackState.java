package ruiseki.okbackpack.common.item.jukebox;

import ruiseki.okbackpack.api.wrapper.IJukeboxUpgrade.JukeboxLoopMode;

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

    public boolean isPlaying() {
        return playing;
    }

    public int getCurrentSlotIndex() {
        return currentSlotIndex;
    }

    public int getProgressTicks() {
        return progressTicks;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public JukeboxLoopMode getLoopMode() {
        return loopMode;
    }
}
