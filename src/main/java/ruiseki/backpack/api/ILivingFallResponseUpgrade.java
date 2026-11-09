package ruiseki.backpack.api;

import net.minecraftforge.event.entity.living.LivingFallEvent;

/**
 * Handles fall distance modifications for a living entity.
 */
public interface ILivingFallResponseUpgrade {

    /**
     * Modifies the fall distance of an entity before fall damage is calculated.
     *
     * @param event the living fall event
     */
    boolean onLivingFall(LivingFallEvent event);
}
