package ruiseki.okbackpack.api;

public interface ILockedStorage {

    String LOCKED_SLOTS_TAG = "LockedSlots";

    boolean isSlotLocked(int slotIndex);

    void setSlotLocked(int slotIndex, boolean locked);

    int getNoSortColorIndex();

    void setNoSortColorIndex(int noSortColorIndex);
}
