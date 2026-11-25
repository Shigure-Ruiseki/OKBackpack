package ruiseki.okbackpack.api.wrapper;

public interface ICompactingUpgrade extends ITickable, IToggleable {

    String ONLY_REVERSIBLE_TAG = "OnlyReversible";

    boolean allowsGrid3x3();

    boolean isOnlyReversible();

    void setOnlyReversible(boolean onlyReversible);

    void compactInventory();
}
