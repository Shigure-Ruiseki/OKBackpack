package ruiseki.okstoragecore.upgrades;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public interface IUpgradeWrapper {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    default boolean canBeDisabled() {
        return true;
    }

    ItemStack getUpgradeStack();

    default boolean hideSettingsTab() {
        return false;
    }

    default void onBeforeRemoved() {
        // noop
    }

    default void onAdded() {
        // noop
    }

    default List<String> getTooltipLines() {
        // noop
        return new ArrayList<>();
    }
}
