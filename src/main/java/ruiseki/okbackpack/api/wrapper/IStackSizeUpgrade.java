package ruiseki.okbackpack.api.wrapper;

public interface IStackSizeUpgrade extends ISlotModifiable {

    double getMultiplier();

    default boolean isDowngrade() {
        return false;
    }

}
