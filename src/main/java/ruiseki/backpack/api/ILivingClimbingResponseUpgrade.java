package ruiseki.backpack.api;

import ruiseki.okcore.event.entity.LivingClimbEvent;

public interface ILivingClimbingResponseUpgrade {

    /**
     * @return true to stop dispatching to subsequent response upgrades
     */
    boolean onLivingClimb(LivingClimbEvent event);
}
