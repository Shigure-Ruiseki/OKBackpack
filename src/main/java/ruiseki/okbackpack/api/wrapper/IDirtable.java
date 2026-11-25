package ruiseki.okbackpack.api.wrapper;

public interface IDirtable {

    boolean isDirty();

    void markDirty();

    void markClean();

    void setDirty(boolean value);
}
