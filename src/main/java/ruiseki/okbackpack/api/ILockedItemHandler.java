package ruiseki.okbackpack.api;

import java.util.List;

public interface ILockedItemHandler {

    boolean isLocked(int slot);

    void setLocked(int slot, boolean locked);

    List<Boolean> getLockedList();
}
