package ruiseki.okbackpack.api;

public interface ILockedStorage {

    boolean isSlotLocked(int slotIndex);

    void setSlotLocked(int slotIndex, boolean locked);

    ILockedItemHandler getLockedItemHandler();
}
