package ruiseki.okbackpack.api.wrapper;

import net.minecraftforge.fluids.FluidStack;

/**
 * Common interface for the basic and advanced pump upgrades. Both transfer fluids between an in-pack
 * {@link ITankUpgrade} and adjacent fluid containers / world fluids / players' hands.
 */
public interface IPumpUpgrade extends ITickable, IToggleable {

    boolean isInput();

    void setInput(boolean input);

    boolean shouldInteractWithHand();

    void setInteractWithHand(boolean interact);

    boolean shouldInteractWithWorld();

    void setInteractWithWorld(boolean interact);

    boolean shouldInteractWithFluidHandlers();

    void setInteractWithFluidHandlers(boolean interact);

    boolean isAdvanced();

    /**
     * Tests whether the given fluid passes the (optional) advanced filter.
     * Basic pumps return {@code true} unconditionally.
     */
    default boolean passesFluidFilter(FluidStack fluidStack) {
        return true;
    }

    /**
     * Whether a tank upgrade is available in the same backpack. Used by the GUI to render the
     * "Requires Tank Upgrade" hint.
     */
    boolean hasTankAvailable();
}
