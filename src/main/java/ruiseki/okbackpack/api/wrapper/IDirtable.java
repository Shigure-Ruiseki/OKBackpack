package ruiseki.okbackpack.api.wrapper;

public interface IDirtable {

    String DIRTY_TAG = "Dirty";

    boolean isDirty();

    void markDirty();

    void markClean();

    void setDirty(boolean value);
}
