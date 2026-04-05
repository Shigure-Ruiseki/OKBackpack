package ruiseki.okbackpack.common.item.wrapper.jukebox;

import java.util.ArrayList;
import java.util.List;

public class JukeboxPlaybackPlanner {

    private JukeboxPlaybackPlanner() {}

    public static List<Integer> buildPlayableSlots(List<Boolean> availability) {
        var result = new ArrayList<Integer>();
        for (int i = 0; i < availability.size(); i++) {
            if (Boolean.TRUE.equals(availability.get(i))) {
                result.add(i);
            }
        }
        return result;
    }

    public static JukeboxPlaybackDecision onInventoryMutation(List<Integer> playableSlots, JukeboxPlaybackState state) {
        if (!state.isPlaying()) {
            return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), state.getProgressTicks());
        }

        if (!playableSlots.contains(state.getCurrentSlotIndex())) {
            return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);
        }

        return new JukeboxPlaybackDecision(true, state.getCurrentSlotIndex(), state.getProgressTicks());
    }

    public static JukeboxPlaybackDecision onTrackFinished(List<Integer> playableSlots, JukeboxPlaybackState state) {
        if (!state.isPlaying() || playableSlots.isEmpty()) {
            return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);
        }

        switch (state.getLoopMode()) {
            case OFF:
                return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);

            case ALL: {
                int currentIdx = playableSlots.indexOf(state.getCurrentSlotIndex());
                int nextIdx;
                if (currentIdx < 0 || currentIdx >= playableSlots.size() - 1) {
                    nextIdx = 0;
                } else {
                    nextIdx = currentIdx + 1;
                }
                return new JukeboxPlaybackDecision(true, playableSlots.get(nextIdx), 0);
            }

            case SINGLE:
                return new JukeboxPlaybackDecision(true, state.getCurrentSlotIndex(), 0);

            default:
                return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);
        }
    }

    public static JukeboxPlaybackDecision onPrevious(List<Integer> playableSlots, JukeboxPlaybackState state) {
        if (!state.isPlaying() || playableSlots.isEmpty()) {
            return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);
        }

        int currentIdx = playableSlots.indexOf(state.getCurrentSlotIndex());
        int prevIdx;
        if (currentIdx <= 0) {
            prevIdx = playableSlots.size() - 1;
        } else {
            prevIdx = currentIdx - 1;
        }
        return new JukeboxPlaybackDecision(true, playableSlots.get(prevIdx), 0);
    }

    public static JukeboxPlaybackDecision onNext(List<Integer> playableSlots, JukeboxPlaybackState state) {
        if (!state.isPlaying() || playableSlots.isEmpty()) {
            return new JukeboxPlaybackDecision(false, state.getCurrentSlotIndex(), 0);
        }

        int currentIdx = playableSlots.indexOf(state.getCurrentSlotIndex());
        int nextIdx;
        if (currentIdx < 0 || currentIdx >= playableSlots.size() - 1) {
            nextIdx = 0;
        } else {
            nextIdx = currentIdx + 1;
        }
        return new JukeboxPlaybackDecision(true, playableSlots.get(nextIdx), 0);
    }
}
