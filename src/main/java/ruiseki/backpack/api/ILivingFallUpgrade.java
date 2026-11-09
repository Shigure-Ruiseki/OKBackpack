package ruiseki.backpack.api;

import net.minecraftforge.event.entity.living.LivingFallEvent;

/**
 * Handles fall distance modifications for a living entity.
 */
public interface ILivingFallUpgrade {

    /**
     * Modifies the fall distance of an entity before fall damage is calculated.
     *
     * @param event the living fall event
     */
    void onLivingFall(LivingFallEvent event);
}
