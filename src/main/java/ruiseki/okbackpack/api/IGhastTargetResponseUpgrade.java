package ruiseki.okbackpack.api;

import ruiseki.okcore.event.entity.GhastTargetEvent;

public interface IGhastTargetResponseUpgrade {

    /**
     * @return true to stop dispatching to subsequent response upgrades
     */
    boolean onGhastTarget(GhastTargetEvent event);
}
