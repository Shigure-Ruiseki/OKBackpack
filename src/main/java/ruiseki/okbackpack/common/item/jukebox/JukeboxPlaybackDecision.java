package ruiseki.okbackpack.common.item.jukebox;

public class JukeboxPlaybackDecision {

    private final boolean playing;
    private final int currentSlotIndex;
    private final int progressTicks;

    public JukeboxPlaybackDecision(boolean playing, int currentSlotIndex, int progressTicks) {
        this.playing = playing;
        this.currentSlotIndex = currentSlotIndex;
        this.progressTicks = progressTicks;
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
}
