package ruiseki.okbackpack.api.wrapper;

public interface ISpiderUpgrade extends IToggleable, ITravelersUpgrade {

    default boolean canClimbWalls() {
        return isEnabled();
    }
}
