package ruiseki.okbackpack.common.item.wrapper.jukebox;

import lombok.Getter;

@Getter
public class JukeboxPlaybackDecision {

    private final boolean playing;
    private final int currentSlotIndex;
    private final int progressTicks;

    public JukeboxPlaybackDecision(boolean playing, int currentSlotIndex, int progressTicks) {
        this.playing = playing;
        this.currentSlotIndex = currentSlotIndex;
        this.progressTicks = progressTicks;
    }
}
