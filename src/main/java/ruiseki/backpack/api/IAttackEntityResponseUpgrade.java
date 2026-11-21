package ruiseki.backpack.api;

import net.minecraftforge.event.entity.player.AttackEntityEvent;

/**
 * Implement if you want your upgrade to be able to respond to AttackEntityEvent event
 */
public interface IAttackEntityResponseUpgrade {

    /**
     * Gets called when a player attacks an entity
     */
    boolean onAttackEntity(AttackEntityEvent event);
}
