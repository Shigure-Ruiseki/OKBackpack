package ruiseki.okbackpack.api.handler;

import java.util.List;

public interface ILockedItemHandler {

    boolean isSlotLocked(int slot);

    void setSlotLocked(int slot, boolean locked);

    List<Boolean> getLockedSlotList();
}
