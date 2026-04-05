package ruiseki.okbackpack.api.wrapper;

public interface IJukeboxUpgrade extends ITickable, IToggleable, IStorageUpgrade {

    String PLAYING_TAG = "Playing";
    String PENDING_STOP_SYNC_TAG = "PendingStopSync";
    String CURRENT_SLOT_INDEX_TAG = "CurrentSlotIndex";
    String PROGRESS_TICKS_TAG = "ProgressTicks";
    String SHUFFLE_TAG = "ShuffleEnabled";
    String LOOP_MODE_TAG = "LoopMode";

    boolean isPlaying();

    void setPlaying(boolean playing);

    int getCurrentSlotIndex();

    void setCurrentSlotIndex(int index);

    int getProgressTicks();

    void setProgressTicks(int ticks);

    void play();

    void stop();

    int getRecordSlotCount();

    enum JukeboxLoopMode {
        OFF,
        ALL,
        SINGLE
    }
}
