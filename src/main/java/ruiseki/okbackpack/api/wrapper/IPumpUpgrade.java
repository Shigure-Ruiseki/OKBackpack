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

    /**
     * Returns the fluid filter type. Only meaningful for advanced pump upgrades.
     */
    default FluidFilterType getFilterType() {
        return FluidFilterType.WHITELIST;
    }

    /**
     * Sets the fluid filter type. Only meaningful for advanced pump upgrades.
     */
    default void setFilterType(FluidFilterType type) {}

    /**
     * Filter mode used by the advanced pump upgrade.
     */
    enum FluidFilterType {

        WHITELIST("gui.backpack.pump.filter_whitelist"),
        BLACKLIST("gui.backpack.pump.filter_blacklist");

        private final String langKey;

        FluidFilterType(String langKey) {
            this.langKey = langKey;
        }

        public String getLangKey() {
            return langKey;
        }

        public FluidFilterType next() {
            FluidFilterType[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public static FluidFilterType fromOrdinal(int ordinal) {
            FluidFilterType[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return WHITELIST;
            }
            return values[ordinal];
        }
    }
}
