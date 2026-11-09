package ruiseki.backpack.api;

import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Implement this interface to allow an upgrade to respond to {@link
 * net.minecraftforge.event.entity.living.LivingHurtEvent}.
 */
public interface ILivingHurtUpgrade {

    /**
     * Modifies the damage amount dealt to an entity.
     *
     * @param event the living hurt event
     */
    void onLivingHurt(LivingHurtEvent event);
}
